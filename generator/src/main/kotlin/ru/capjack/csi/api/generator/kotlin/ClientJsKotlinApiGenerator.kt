package ru.capjack.csi.api.generator.kotlin

import ru.capjack.csi.api.generator.model.Api
import ru.capjack.csi.api.generator.model.ApiModel
import ru.capjack.csi.api.generator.model.ServiceDescriptor
import ru.capjack.tool.io.biser.generator.CodeBlock
import ru.capjack.tool.io.biser.generator.CodeFile
import ru.capjack.tool.io.biser.generator.CodePath
import ru.capjack.tool.io.biser.generator.ImportsCollection
import ru.capjack.tool.io.biser.generator.kotlin.KotlinCodersGenerator
import ru.capjack.tool.io.biser.generator.kotlin.KotlinFile
import ru.capjack.tool.io.biser.generator.model.EntityDescriptor
import ru.capjack.tool.io.biser.generator.model.EnumDescriptor
import ru.capjack.tool.io.biser.generator.model.ObjectDescriptor
import ru.capjack.tool.io.biser.generator.model.StructureDescriptor
import ru.capjack.tool.io.biser.generator.model.StructureDescriptorVisitor
import ru.capjack.tool.io.biser.generator.model.StructureType
import ru.capjack.tool.io.biser.generator.model.Type
import java.nio.file.Path

class TsFile(private val name: String) : CodeFile() {
	override fun write(path: Path) {
		super.write(path.resolve("$name.ts"))
	}
}

class ClientJsKotlinApiGenerator(targetPackage: CodePath, coders: KotlinCodersGenerator) : ClientKotlinApiGenerator(targetPackage, coders) {
	
	override fun generate(model: ApiModel, targetSrc: Path) {
		super.generate(model, targetSrc)
		
		val files = mutableListOf<TsFile>()
		val tsPath = targetSrc.resolveSibling("typescript")
		
		tsPath.toFile().deleteRecursively()
		
		files.add(generateTsData(model))
		
		files.forEach { it.write(tsPath) }
	}
	
	private fun generateTsData(model: ApiModel): TsFile {
		return TsFile("data").apply {
			identBracketsCurly("module csi.data ") {
				model.structures.forEach {
					it.accept(tsStructureGenerator, this)
					line()
				}
			}
		}
	}
	
	private val tsStructureGenerator = object : StructureDescriptorVisitor<Unit, CodeBlock> {
		override fun visitEntityStructureDescriptor(descriptor: EntityDescriptor, data: CodeBlock) {
			val parent = descriptor.parent
			data.identBracketsCurly("export class ${descriptor.type.path.name} ${if (parent == null) "" else "extends ${parent.path.name}"}") {
			
			}
		}
		
		override fun visitEnumStructureDescriptor(descriptor: EnumDescriptor, data: CodeBlock) {
			data.identBracketsCurly("export enum ${descriptor.type.path.name}") {
				descriptor.values.forEach {
					line("${it.name},")
				}
			}
		}
		
		override fun visitObjectStructureDescriptor(descriptor: ObjectDescriptor, data: CodeBlock) {
			data.identBracketsCurly("export class ${descriptor.type.path.name}") {
			}
		}
	}
	
	override fun generate(model: ApiModel, files: MutableList<CodeFile>) {
		super.generate(model, files)
		
		files.add(generateKotlinApi(model.client))
		files.add(generateKotlinApi(model.server))
		
		model.client.services.forEach { files.add(generateKotlinService(it.descriptor)) }
		model.server.services.forEach { files.add(generateKotlinService(it.descriptor)) }
		
		model.structures.forEach {
			
			val name = targetPackage.resolve(it.type.path)
			val file = KotlinFile(name)
			file.addAnnotation("JsQualifier(\"${name.parent}\")")
			it.accept(kotlinStructureGenerator, file)
			files.add(file)
		}
		
		files.add(generateKotlinConnector())
	}
	
	private fun generateKotlinConnector(): CodeFile {
		return KotlinFile(targetPackage.resolve("client.connect")).apply {
			addImport("ru.capjack.csi.api.client.AbstractJsConnector")
			addImport("ru.capjack.csi.api.client.ApiSluice")
			addImport("ru.capjack.csi.api.client.JsApiSluice")
			addImport("ru.capjack.csi.core.client.ConnectionAcceptor")
			addImport("ru.capjack.tool.io.ByteBuffer")
			addImport("ru.capjack.tool.utils.pool.ObjectPool")
			
			line("@JsName(\"connect\")")
			identBracketsCurly("fun connect(errorHandler: (dynamic) -> Unit, sluice: JsApiSluice<InternalClientApi, InternalServerApi>, url: String, authKey: String) ") {
				line("Connector(errorHandler, url, sluice).connect(authKey)")
			}
			
			line()
			line("internal class Connector(errorHandler: (dynamic) -> Unit, url: String, sluice: JsApiSluice<InternalClientApi, InternalServerApi>) :")
			identLine("AbstractJsConnector<InternalClientApi, InternalServerApi>(errorHandler, url, sluice) {")
			identLine("override fun createApiAdapter(sluice: ApiSluice<InternalClientApi, InternalServerApi>, byteBuffers: ObjectPool<ByteBuffer>): ConnectionAcceptor {")
			ident { identLine("return ApiAdapter(sluice, byteBuffers)") }
			identLine("}")
			line("}")
		}
	}
	
	private val kotlinStructureGenerator = object : StructureDescriptorVisitor<Unit, KotlinFile> {
		override fun visitEntityStructureDescriptor(descriptor: EntityDescriptor, data: KotlinFile) {
			data.apply {
				
				line {
					if (descriptor.abstract) {
						append("abstract ")
					}
					else if (descriptor.children.isNotEmpty()) {
						append("open ")
					}
					append("external class ${descriptor.type.path.name} (")
				}
				
				ident {
					descriptor.fields.forEach { field ->
						line {
							append("${field.name}: ${field.type.toString(this@apply)},")
						}
					}
				}
				
				val parent = descriptor.parent
				val parentFields = (parent?.descriptor as EntityDescriptor?)?.fields ?: emptyList()
				
				val tmp = if (parent != null) {
					addImport(targetPackage.resolve(parent.path))
					"): ${parent.path.name} "
				}
				else {
					") "
				}
				
				identBracketsCurly(tmp) {
					descriptor.fields
						.filter { field -> parentFields.none { it.name == field.name } }
						.forEach { field ->
							line("val ${field.name}: ${field.type.toString(this@apply)}")
						}
				}
			}
			
			
		}
		
		override fun visitEnumStructureDescriptor(descriptor: EnumDescriptor, data: KotlinFile) {
			data.identBracketsCurly("external enum class ${descriptor.type.path.name} ") {
				descriptor.values.forEach { value ->
					line {
						append("${value.name},")
					}
				}
			}
		}
		
		override fun visitObjectStructureDescriptor(descriptor: ObjectDescriptor, data: KotlinFile) {
			data.line("external object ${descriptor.type.path.name} ")
		}
	}
	
	
	private fun generateKotlinService(service: ServiceDescriptor): CodeFile {
		return KotlinFile(targetPackage.resolve(service.path)).apply {
			addAnnotation("JsQualifier(\"${targetPackage.resolve(service.path).parent}\")")
			
			identBracketsCurly("external interface ${service.path.name} ") {
				service.methods.forEach { method ->
					line {
						append("fun ")
						append(method.name)
						append('(')
						
						method.arguments.joinTo(this) {
							it.name + ": " + it.type.toString(this@apply)
						}
						
						method.result?.also { result ->
							if (method.arguments.isNotEmpty()) {
								append(", ")
							}
							append("callback: (")
							result.joinTo(this) { p ->
								(p.name?.let { "$it: " } ?: "") + p.type.toString(this@apply)
							}
							append(") -> Unit")
						}
						
						append(')')
					}
				}
			}
		}
	}
	
	private fun generateKotlinApi(api: Api): CodeFile {
		return KotlinFile(targetPackage.resolve(api.path)).apply {
			addAnnotation("JsQualifier(\"${targetPackage.resolve(api.path).parent}\")")
			
			identBracketsCurly("external interface ${api.path.name} ") {
				api.services.forEach { service ->
					addImport(targetPackage.resolve(service.descriptor.path))
					line("val ${service.name}: ${service.descriptor.path.name}")
				}
			}
		}
	}
	
	private fun Type.toString(imports: ImportsCollection): String {
		return coders.getTypeName(imports, this)
	}
	
	private fun StructureDescriptor.tsName(): String {
		return type.tsName()
	}
	
	private fun StructureType.tsName(): String {
		return path.name.replace('.', '_')
	}
}