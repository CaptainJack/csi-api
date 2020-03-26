package ru.capjack.csi.api.generator.kotlin

import ru.capjack.csi.api.generator.model.Api
import ru.capjack.csi.api.generator.model.ApiModel
import ru.capjack.csi.api.generator.model.Method
import ru.capjack.csi.api.generator.model.Parameter
import ru.capjack.csi.api.generator.model.Service
import ru.capjack.csi.api.generator.model.ServiceDescriptor
import ru.capjack.tool.io.biser.generator.CodeFile
import ru.capjack.tool.io.biser.generator.CodePath
import ru.capjack.tool.io.biser.generator.kotlin.KotlinCodersGenerator
import ru.capjack.tool.io.biser.generator.kotlin.KotlinFile
import ru.capjack.tool.io.biser.generator.model.PrimitiveType
import java.nio.file.Path

abstract class AbstractKotlinApiGenerator(
	private val coders: KotlinCodersGenerator,
	protected val targetPackage: CodePath,
	protected val side: String
) {
	
	private val sidePackage = targetPackage.resolve(side)
	
	abstract fun generate(model: ApiModel, targetSrc: Path)
	
	protected abstract fun generateApiAdapter(file: KotlinFile, innerApi: Api, outerApi: Api)
	
	protected fun generate(innerApi: Api, outerApi: Api, targetSrc: Path) {
		targetSrc.toFile().deleteRecursively()
		
		val files = mutableListOf(
			generateInnerApi(innerApi),
			generateOuterApi(outerApi),
			generateApiAdapter(innerApi, outerApi),
			generateApiConnection(innerApi)
		)
		
		outerApi.services.sortedBy(Service::id).mapTo(files) { generateOuterApiService(it.descriptor) }
		
		files.forEach { it.write(targetSrc) }
	}
	
	private fun generateApiAdapter(innerApi: Api, outerApi: Api): CodeFile {
		return KotlinFile(sidePackage.resolve("ApiAdapter")).apply {
			addImport("ru.capjack.csi.api.CallbacksRegister")
			addImport("ru.capjack.csi.api.$side.AbstractApiAdapter")
			addImport("ru.capjack.csi.api.$side.ApiSluice")
			addImport("ru.capjack.csi.core.Connection")
			addImport("ru.capjack.csi.core.$side.ConnectionHandler")
			addImport("ru.capjack.tool.io.ByteBuffer")
			addImport("ru.capjack.tool.utils.concurrency.ObjectPool")
			addImport(targetPackage.resolve(innerApi.path))
			addImport(targetPackage.resolve(outerApi.path))
			
			generateApiAdapter(this, innerApi, outerApi)
		}
	}
	
	private fun generateInnerApi(api: Api): CodeFile {
		val name = "Internal${api.path.name}"
		
		return KotlinFile(sidePackage.resolve(name)).apply {
			addImport("ru.capjack.csi.api.$side.InternalApi")
			addImport(targetPackage.resolve(api.path))
			
			line("interface $name : ${api.path.name}, InternalApi")
		}
	}
	
	private fun generateOuterApi(api: Api): CodeFile {
		val name = "${api.path.name}Impl"
		
		return KotlinFile(sidePackage.resolve(name)).apply {
			addImport("ru.capjack.csi.api.CallbacksRegister")
			addImport("ru.capjack.csi.api.OutputApiMessage")
			addImport("ru.capjack.csi.core.Connection")
			addImport("ru.capjack.tool.utils.concurrency.ObjectPool")
			addImport(targetPackage.resolve(api.path))
			
			identBracketsCurly("internal class $name(writers: ObjectPool<OutputApiMessage>, connection: Connection, callbacks: CallbacksRegister): ${api.path.name}") {
				api.services.sortedBy(Service::id).forEach {
					addImport(targetPackage.resolve(it.descriptor.path))
					line("override val ${it.name}: ${it.descriptor.path.name} = ${it.descriptor.path.name}Impl(${it.id}, writers, connection, callbacks)")
				}
			}
		}
	}
	
	private fun generateOuterApiService(descriptor: ServiceDescriptor): CodeFile {
		val name = descriptor.path.name
		
		return KotlinFile(sidePackage.resolve("${name}Impl")).apply {
			addImport(targetPackage.resolve(descriptor.path))
			addImport("ru.capjack.csi.api.AbstractService")
			addImport("ru.capjack.csi.api.CallbacksRegister")
			addImport("ru.capjack.csi.api.OutputApiMessage")
			addImport("ru.capjack.csi.core.Connection")
			addImport("ru.capjack.tool.utils.concurrency.ObjectPool")
			
			line("internal class ${name}Impl(")
			
			identLine("serviceId: Int,")
			identLine("writers: ObjectPool<OutputApiMessage>,")
			identLine("connection: Connection,")
			identLine("callbacks: CallbacksRegister")
			
			identBracketsCurly(") : AbstractService(serviceId, writers, connection, callbacks), $name ") {
				descriptor.methods.sortedBy(Method::id).forEach { m ->
					
					line()
					line {
						append("override fun ${m.name}(")
						append(m.arguments.joinToString { it.name + ": " + coders.getTypeName(this@apply, it.type) })
						m.result?.also {
							if (m.arguments.isNotEmpty()) {
								append(", ")
							}
							val params = m.result!!.joinToString { p -> (p.name?.let { "$it: " } ?: "") + coders.getTypeName(this@apply, p.type) }
							append("receiver: ($params) -> Unit")
						}
						append(") {")
					}
					
					ident {
						
						val block = m.result
							?.let { r ->
								line("send(${m.id}, {")
								ident {
									r.forEachIndexed { i, a ->
										line("val p$i = " + coders.provideReadCall(this@apply, a.type))
									}
									line("receiver(" + r.indices.joinToString { "p$it" } + ")")
								}
								identBracketsCurly("}) ")
							}
							?: identBracketsCurly("send(${m.id}) ")
						
						
						m.arguments.forEach { a ->
							block.line(coders.provideWriteCall(this@apply, a.type, a.name!!))
						}
						
					}
					line("}")
				}
			}
		}
	}
	
	private fun generateApiConnection(api: Api): CodeFile {
		val iaInternalName = "Internal${api.path.name}"
		
		return KotlinFile(sidePackage.resolve("ApiConnection")).apply {
			addImport("ru.capjack.csi.api.CallbacksRegister")
			addImport("ru.capjack.csi.api.ApiMessagePool")
			addImport("ru.capjack.csi.api.$side.AbstractApiConnection")
			addImport("ru.capjack.csi.core.Connection")
			addImport("ru.capjack.tool.io.biser.BiserReader")
			
			line("internal class ApiConnection(")
			ident {
				line("messagePool: ApiMessagePool,")
				line("connection: Connection,")
				line("callbacks: CallbacksRegister,")
				line("api: $iaInternalName")
			}
			identBracketsCurly(") : AbstractApiConnection<$iaInternalName>(messagePool, connection, callbacks, api) ") {
				
				val services = api.services.sortedBy(Service::id)
				
				line()
				identBracketsCurly("override fun call(serviceId: Int, methodId: Int, message: BiserReader): Boolean ") {
					identBracketsCurly("return when (serviceId) ") {
						services.forEach {
							line("${it.id} -> call(api.${it.name}, methodId, message)")
						}
						line("else -> false")
					}
				}
				
				services.map { it.descriptor }.forEach { s ->
					addImport(targetPackage.resolve(s.path))
					
					line()
					identBracketsCurly("private fun call(service: ${s.path.name}, methodId: Int, message: BiserReader): Boolean ") {
						identBracketsCurly("when (methodId) ") {
							s.methods.sortedBy(Method::id).forEach { m ->
								identBracketsCurly("${m.id} -> ") {
									val hasResult = m.result != null
									val hasArguments = m.arguments.isNotEmpty()
									if (hasResult) {
										line("val r = message." + coders.provideReadCall(this@apply, PrimitiveType.INT))
									}
									m.arguments.forEachIndexed { i, a ->
										line("val a$i = message." + coders.provideReadCall(this@apply, a.type))
									}
									
									line {
										append("service.${m.name}")
										
										if (hasArguments) {
											append('(')
											append(m.arguments.indices.joinToString { "a$it" })
											append(')')
										}
										
										if (hasResult) {
											append(" { ")
											append(m.result!!.indices.joinToString { "r$it" })
											append(" ->")
										}
										else if (!hasArguments) {
											append("()")
										}
									}
									
									if (hasResult) {
										ident {
											identBracketsCurly("sendResponse(r) ") {
												m.result!!.forEachIndexed { i, p ->
													line(coders.provideWriteCall(this@apply, p.type, "r$i"))
												}
											}
										}
										line("}")
									}
								}
							}
						}
						line("return true")
					}
				}
			}
		}
	}
}
