package ru.capjack.csi.api.generator.kotlin

import ru.capjack.csi.api.generator.model.Api
import ru.capjack.csi.api.generator.model.ApiModel
import ru.capjack.tool.io.biser.generator.CodePath
import ru.capjack.tool.io.biser.generator.kotlin.KotlinCodersGenerator
import ru.capjack.tool.io.biser.generator.kotlin.KotlinFile
import java.nio.file.Path

class ServerKotlinApiGenerator(
	targetPackage: CodePath,
	coders: KotlinCodersGenerator
) : AbstractKotlinApiGenerator(coders, targetPackage, "server") {
	
	override fun generate(model: ApiModel, targetSrc: Path) {
		generate(model.server, model.client, targetSrc)
	}
	
	override fun generateApiAdapter(file: KotlinFile, innerApi: Api, outerApi: Api) {
		val iaInternalName = "Internal${innerApi.path.name}"
		val oaName = outerApi.path.name
		
		file.apply {
			line("class ApiAdapter<I : Any>(")
			ident {
				line("sluice: ApiSluice<I, $iaInternalName, $oaName>,")
				line("byteBuffers: ObjectPool<ByteBuffer>")
			}
			identBracketsCurly(") : AbstractApiAdapter<I, $iaInternalName, $oaName>(sluice, byteBuffers) ") {
				line()
				identBracketsCurly("override fun createConnectionHandler(connection: Connection, callbacks: CallbacksRegister, api: $iaInternalName): ConnectionHandler ") {
					line("return ApiConnection(messagePool, connection, callbacks, api)")
				}
				
				line()
				identBracketsCurly("override fun createOuterApi(connection: Connection, callbacks: CallbacksRegister): $oaName ") {
					line("return ${oaName}Impl(messagePool.writers, connection, callbacks)")
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
}