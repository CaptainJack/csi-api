package ru.capjack.csi.api.generator.kotlin

import ru.capjack.csi.api.generator.model.Api
import ru.capjack.csi.api.generator.model.ApiModel
import ru.capjack.csi.api.generator.model.Method
import ru.capjack.csi.api.generator.model.Service
import ru.capjack.csi.api.generator.model.ServiceDescriptor
import ru.capjack.tool.io.biser.generator.CodeBlock
import ru.capjack.tool.io.biser.generator.CodeFile
import ru.capjack.tool.io.biser.generator.CodePath
import ru.capjack.tool.io.biser.generator.CoderNameScopeVisitor
import ru.capjack.tool.io.biser.generator.ImportsCollection
import ru.capjack.tool.io.biser.generator.TypeAggregator
import ru.capjack.tool.io.biser.generator.TypeCollector
import ru.capjack.tool.io.biser.generator.kotlin.KotlinCoderNameVisitor
import ru.capjack.tool.io.biser.generator.kotlin.KotlinCodersGenerator
import ru.capjack.tool.io.biser.generator.kotlin.KotlinFile
import ru.capjack.tool.io.biser.generator.model.*
import java.nio.file.Path

abstract class KotlinApiGenerator(
	protected val coders: KotlinCodersGenerator,
	protected val targetPackage: CodePath,
	private val side: String
) {
	private class LogCallVisitorData(
		val loggers: TypeCollector,
		val imports: ImportsCollection,
		val code: CodeBlock,
		val argName: String?,
		val argVal: String,
		val sep: Boolean
	)
	
	private class GenerateLoggingVisitorData(val imports: ImportsCollection, val code: CodeBlock, val loggers: TypeCollector)
	
	private val sidePackage = targetPackage.resolve(side)
	
	private val logNamesVisitor = KotlinCoderNameVisitor(object : CoderNameScopeVisitor {
		override fun visitPrimitiveScope(name: String) = name
		override fun visitGeneratedScope(name: String) = name
	})
	
	private val logCallVisitor = object : TypeVisitor<Unit, LogCallVisitorData> {
		
		private fun defineFn(data: LogCallVisitorData): String {
			return if (data.sep) {
				data.imports.addImport("ru.capjack.csi.api.logS")
				"logS"
			}
			else {
				data.imports.addImport("ru.capjack.csi.api.log")
				"log"
			}
		}
		
		private fun visitGenerated(type: Type, data: LogCallVisitorData) {
			val fn = defineFn(data)
			val logger = defineLogName(type)
			data.loggers.add(type)
			
			if (data.argName == null) {
				data.code.line("$fn(${data.argVal}, $logger)")
			}
			else {
				data.code.line("$fn(\"${data.argName}\", ${data.argVal}, $logger)")
			}
		}
		
		override fun visitPrimitiveType(type: PrimitiveType, data: LogCallVisitorData) {
			val fn = defineFn(data)
			if (data.argName == null) {
				data.code.line("$fn(${data.argVal})")
			}
			else {
				data.code.line("$fn(\"${data.argName}\", ${data.argVal})")
			}
		}
		
		override fun visitListType(type: ListType, data: LogCallVisitorData) {
			visitGenerated(type.element, data)
		}
		
		override fun visitNullableType(type: NullableType, data: LogCallVisitorData) {
			visitGenerated(type, data)
		}
		
		override fun visitStructureType(type: StructureType, data: LogCallVisitorData) {
			if (type.descriptor is EnumDescriptor) {
				val fn = defineFn(data)
				if (data.argName == null) {
					data.code.line("$fn(${data.argVal}.name)")
				}
				else {
					data.code.line("$fn(\"${data.argName}\", ${data.argVal}.name)")
				}
			}
			else {
//				(type.descriptor as? EntityDescriptor)?.also { visitEntityDescriptor(it, data) }
				
				visitGenerated(type, data)
			}
		}
		
//		private fun visitEntityDescriptor(descriptor: EntityDescriptor, data: LogCallVisitorData) {
//			data.loggers.add(descriptor.type)
//			descriptor.children.forEach { visitEntityDescriptor(it.descriptor as EntityDescriptor, data) }
//		}
	}
	
	private val loggingVisitor = object : TypeVisitor<Unit, GenerateLoggingVisitorData>, StructureDescriptorVisitor<Unit, GenerateLoggingVisitorData> {
		override fun visitPrimitiveType(type: PrimitiveType, data: GenerateLoggingVisitorData) {
			data.imports.addImport("ru.capjack.csi.api.log")
			data.code.line("log(it)")
		}
		
		override fun visitListType(type: ListType, data: GenerateLoggingVisitorData) {
			data.loggers.add(type.element)
			data.code.line("log(it, ${defineLogName(type.element)})")
		}
		
		override fun visitNullableType(type: NullableType, data: GenerateLoggingVisitorData) {
			data.loggers.add(type.original)
			data.code.line("if (it == null) append(\"NULL\") else log(it, ${defineLogName(type.original)})")
		}
		
		override fun visitStructureType(type: StructureType, data: GenerateLoggingVisitorData) {
			type.descriptor.accept(this, data)
		}
		
		override fun visitEnumStructureDescriptor(descriptor: EnumDescriptor, data: GenerateLoggingVisitorData) {
			data.code.line("log(it.name)")
		}
		
		override fun visitEntityStructureDescriptor(descriptor: EntityDescriptor, data: GenerateLoggingVisitorData) {
			data.code.apply {
				if (descriptor.children.isEmpty()) {
					visitEntityStructureDescriptor0(descriptor, data)
				}
				else {
					identBracketsCurly("when (it) ") {
						descriptor.children.forEach {
							data.loggers.add(it)
							line("is ${coders.getTypeName(data.imports, it)} -> ${defineLogName(it)}(it)")
						}
						if (descriptor.abstract) {
							line("else -> throw UnsupportedOperationException()")
						}
						else {
							identBracketsCurly("else -> ") {
								visitEntityStructureDescriptor0(descriptor, data)
							}
						}
					}
				}
			}
		}
		
		private fun CodeBlock.visitEntityStructureDescriptor0(descriptor: EntityDescriptor, data: GenerateLoggingVisitorData) {
			line("append('{')")
			descriptor.fields.forEachIndexed { i, f ->
				logCall(data.loggers, data.imports, descriptor.fields.lastIndex == i, f.type, f.name, "it.${f.name}")
			}
			line("append('}')")
		}
		
		override fun visitObjectStructureDescriptor(descriptor: ObjectDescriptor, data: GenerateLoggingVisitorData) {
			data.code.line("log(\"${descriptor.type.path.name}\")")
		}
	}
	
	open fun generate(model: ApiModel, targetSrc: Path) {
		targetSrc.toFile().deleteRecursively()
		
		val files = mutableListOf<CodeFile>()
		generate(model, files)
		
		files.forEach { it.write(targetSrc) }
	}
	
	protected abstract fun generate(model: ApiModel, files: MutableList<CodeFile>)
	
	protected open fun generate(innerApi: Api, outerApi: Api, files: MutableList<CodeFile>) {
		
		val loggers = TypeAggregator()
		
		files.add(generateInnerApi(innerApi))
		files.add(generateOuterApi(outerApi))
		files.add(generateOuterApiImpl(outerApi))
		files.add(generateApiAdapter(innerApi, outerApi))
		files.add(generateApiConnection(innerApi, loggers))
		
		outerApi.services.sortedBy(Service::id).mapTo(files) { generateOuterApiService(it.descriptor, loggers) }
		
		if (loggers.hasNext()) {
			files.add(generateLogging(loggers))
		}
	}
	
	protected abstract fun generateApiAdapterDeclaration(code: CodeBlock, iaName: String, oaName: String): CodeBlock
	
	private fun generateApiAdapter(innerApi: Api, outerApi: Api): CodeFile {
		return KotlinFile(sidePackage.resolve("ApiAdapter")).apply {
			addImport("ru.capjack.csi.api.CallbacksRegister")
			addImport("ru.capjack.csi.api.$side.AbstractApiAdapter")
			addImport("ru.capjack.csi.api.$side.ApiSluice")
			addImport("ru.capjack.csi.core.Connection")
			addImport("ru.capjack.csi.core.$side.ConnectionHandler")
			addImport("ru.capjack.tool.io.ByteBuffer")
			addImport("ru.capjack.tool.logging.Logging")
			addImport("ru.capjack.tool.utils.pool.ObjectPool")
			
			
			val iaName = "Internal" + innerApi.path.name
			val oaName = "Internal" + outerApi.path.name
			
			generateApiAdapterDeclaration(this, iaName, oaName).apply {
				line()
				line("private val logger = Logging.getLogger(\"${sidePackage.value}\")")
				
				line()
				identBracketsCurly("override fun createConnectionHandler(connection: Connection, callbacks: CallbacksRegister, api: $iaName): ConnectionHandler ") {
					line("return ApiConnection(logger, messagePool, connection, callbacks, api)")
				}
				
				line()
				identBracketsCurly("override fun createOuterApi(connection: Connection, callbacks: CallbacksRegister): $oaName ") {
					line("return ${oaName}Impl(logger, messagePool.writers, connection, callbacks)")
				}
				
				line()
				identBracketsCurly("override fun provideCallbacksRegister(): CallbacksRegister ") {
					if (outerApi.services.any { s -> s.descriptor.methods.any { it.result != null } }) {
						addImport("ru.capjack.csi.api.RealCallbacksRegister")
						line("return RealCallbacksRegister()")
					}
					else {
						addImport("ru.capjack.csi.api.NothingCallbacksRegister")
						line("return NothingCallbacksRegister")
					}
				}
			}
			
		}
	}
	
	private fun generateInnerApi(api: Api): CodeFile {
		val name = "Internal${api.path.name}"
		
		return KotlinFile(sidePackage.resolve(name)).apply {
			addImport("ru.capjack.csi.api.$side.InnerApi")
			addImport(targetPackage.resolve(api.path))
			
			line("interface $name : ${api.path.name}, InnerApi")
		}
	}
	
	private fun generateOuterApi(api: Api): CodeFile {
		val name = "Internal${api.path.name}"
		
		return KotlinFile(sidePackage.resolve(name)).apply {
			addImport("ru.capjack.csi.api.OuterApi")
			addImport(targetPackage.resolve(api.path))
			
			line("interface $name : ${api.path.name}, OuterApi")
		}
	}
	
	private fun generateOuterApiImpl(api: Api): CodeFile {
		val name = "Internal${api.path.name}Impl"
		
		return KotlinFile(sidePackage.resolve(name)).apply {
			addImport("ru.capjack.csi.api.AbstractOuterApi")
			addImport("ru.capjack.csi.api.CallbacksRegister")
			addImport("ru.capjack.csi.api.OutputApiMessage")
			addImport("ru.capjack.csi.core.Connection")
			addImport("ru.capjack.tool.logging.Logger")
			addImport("ru.capjack.tool.utils.pool.ObjectPool")
			
			line("internal class $name(")
			ident {
				line("logger: Logger,")
				line("writers: ObjectPool<OutputApiMessage>,")
				line("connection: Connection,")
				line("callbacks: CallbacksRegister")
			}
			identBracketsCurly("): AbstractOuterApi(connection), Internal${api.path.name} ") {
				api.services.sortedBy(Service::id).forEach {
					addImport(targetPackage.resolve(it.descriptor.path))
					line("override val ${it.name}: ${it.descriptor.path.name} = ${it.descriptor.path.name}Impl(${it.id}, \"${it.name}\", logger, writers, connection, callbacks)")
				}
			}
		}
	}
	
	private fun generateOuterApiService(descriptor: ServiceDescriptor, loggers: TypeCollector): CodeFile {
		val name = descriptor.path.name
		
		return KotlinFile(sidePackage.resolve("${name}Impl")).apply {
			addImport(targetPackage.resolve(descriptor.path))
			addImport("ru.capjack.csi.api.AbstractService")
			addImport("ru.capjack.csi.api.CallbacksRegister")
			addImport("ru.capjack.csi.api.OutputApiMessage")
			addImport("ru.capjack.csi.core.Connection")
			addImport("ru.capjack.tool.logging.Logger")
			addImport("ru.capjack.tool.utils.pool.ObjectPool")
			
			line("internal class ${name}Impl(")
			ident {
				line("serviceId: Int,")
				line("serviceName: String,")
				line("logger: Logger,")
				line("writers: ObjectPool<OutputApiMessage>,")
				line("connection: Connection,")
				line("callbacks: CallbacksRegister")
			}
			
			identBracketsCurly(") : AbstractService(serviceId, serviceName, logger, writers, connection, callbacks), $name ") {
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
							append("callback: ($params) -> Unit")
						}
						append(") {")
					}
					
					ident {
						val r = m.result
						val send: String
						val log: String
						
						if (r == null) {
							send = "send(${m.id})"
							log = "logSend(\"${m.name}\") "
						}
						else {
							send = "send(${m.id}, c)"
							log = "logSend(\"${m.name}\", c) "
							
							identBracketsCurly("val c = registerCallback ") {
								r.forEachIndexed { i, a ->
									line("val p$i = " + coders.provideReadCall(this@apply, a.type))
								}
								
								identBracketsCurly("logCallback(\"${m.name}\", it) ") {
									r.forEachIndexed { i, a ->
										logCall(loggers, this@apply, r.lastIndex == i, a.type, a.name, "p$i")
									}
								}
								
								line("callback(" + r.indices.joinToString { "p$it" } + ")")
							}
						}
						
						identBracketsCurly(log) {
							m.arguments.forEachIndexed { i, a ->
								logCall(loggers, this@apply, m.arguments.lastIndex == i, a.type, a.name)
							}
						}
						
						if (m.arguments.isEmpty()) {
							line(send)
						}
						else {
							identBracketsCurly("$send ") {
								m.arguments.forEach { a ->
									line(coders.provideWriteCall(this@apply, a.type, a.name!!))
								}
							}
						}
						
					}
					line("}")
				}
			}
		}
	}
	
	private fun generateApiConnection(api: Api, loggers: TypeCollector): CodeFile {
		val iaInternalName = "Internal${api.path.name}"
		
		return KotlinFile(sidePackage.resolve("ApiConnection")).apply {
			addImport("ru.capjack.csi.api.CallbacksRegister")
			addImport("ru.capjack.csi.api.ApiMessagePool")
			addImport("ru.capjack.csi.api.$side.AbstractApiConnection")
			addImport("ru.capjack.csi.core.Connection")
			addImport("ru.capjack.tool.io.biser.BiserReader")
			addImport("ru.capjack.tool.logging.Logger")
			
			line("internal class ApiConnection(")
			ident {
				line("logger: Logger,")
				line("messagePool: ApiMessagePool,")
				line("connection: Connection,")
				line("callbacks: CallbacksRegister,")
				line("api: $iaInternalName")
			}
			identBracketsCurly(") : AbstractApiConnection<$iaInternalName>(logger, messagePool, connection, callbacks, api) ") {
				
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
				
				services.forEach { s ->
					val d = s.descriptor
					addImport(targetPackage.resolve(d.path))
					
					line()
					identBracketsCurly("private fun call(service: ${d.path.name}, methodId: Int, message: BiserReader): Boolean ") {
						identBracketsCurly("when (methodId) ") {
							d.methods.sortedBy(Method::id).forEach { m ->
								identBracketsCurly("${m.id} -> ") {
									val result = m.result
									val arguments = m.arguments
									val hasArguments = arguments.isNotEmpty()
									if (result != null) {
										line("val r = message." + coders.provideReadCall(this@apply, PrimitiveType.INT))
									}
									arguments.forEachIndexed { i, a ->
										line("val a$i = message." + coders.provideReadCall(this@apply, a.type))
									}
									
									
									line {
										append("logReceive(\"${s.name}\", \"${m.name}\"")
										if (result != null) {
											append(", r")
										}
										append(") {")
									}
									ident {
										arguments.forEachIndexed { i, a ->
											logCall(loggers, this@apply, i == arguments.lastIndex, a.type, a.name, "a$i")
										}
									}
									line("}")
									
									line {
										append("service.${m.name}")
										
										if (hasArguments) {
											append('(')
											append(arguments.indices.joinToString { "a$it" })
											append(')')
										}
										
										if (result != null) {
											append(" { ")
											append(result.indices.joinToString { "r$it" })
											append(" ->")
										}
										else if (!hasArguments) {
											append("()")
										}
									}
									
									if (result != null) {
										ident {
											identBracketsCurly("logCallback(\"${s.name}\", \"${m.name}\", r) ") {
												result.forEachIndexed { i, a ->
													logCall(loggers, this@apply, i == result.lastIndex, a.type, a.name, "r$i")
												}
											}
											
											identBracketsCurly("sendResponse(r) ") {
												result.forEachIndexed { i, p ->
													line(coders.provideWriteCall(this@apply, p.type, "r$i"))
												}
											}
										}
										line("}")
									}
								}
							}
							line("else -> return false")
						}
						line("return true")
					}
				}
			}
		}
	}
	
	private fun defineLogName(type: Type): String {
		return "LOG_" + type.accept(logNamesVisitor)
	}
	
	private fun generateLogging(loggers: TypeAggregator): CodeFile {
		return KotlinFile(sidePackage.resolve("_logging")).apply {
			loggers.forEach { type ->
				val name = defineLogName(type)
				identBracketsCurly("internal val $name: StringBuilder.(${coders.getTypeName(this, type)}) -> Unit = ") {
					type.accept(loggingVisitor, GenerateLoggingVisitorData(this@apply, this, loggers))
				}
				line()
			}
		}
	}
	
	private fun CodeBlock.logCall(loggers: TypeCollector, imports: ImportsCollection, last: Boolean, type: Type, argName: String?, valName: String = argName!!) {
		type.accept(logCallVisitor, LogCallVisitorData(loggers, imports, this, argName, valName, !last))
	}
}
