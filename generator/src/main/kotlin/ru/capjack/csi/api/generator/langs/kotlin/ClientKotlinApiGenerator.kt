package ru.capjack.csi.api.generator.langs.kotlin

import ru.capjack.csi.api.generator.model.ApiModel
import ru.capjack.tool.biser.generator.Code
import ru.capjack.tool.biser.generator.langs.kotlin.KotlinCodeSource
import ru.capjack.tool.biser.generator.langs.kotlin.KotlinCodersGenerator

open class ClientKotlinApiGenerator(
	model: ApiModel,
	coders: KotlinCodersGenerator,
	targetPackage: String
) : KotlinApiGenerator(model, coders, targetPackage, "client") {
	
	override fun generate(codeSource: KotlinCodeSource) {
		generate(model.client, model.server, codeSource)
	}
	
	override fun generateApiAdapterDeclaration(code: Code, iaName: String, oaName: String): Code {
		code.line("class ApiAdapter(")
		code.ident {
			line("sluice: ApiSluice<$iaName, $oaName>,")
			line("coroutineScope: CoroutineScope,")
			line("byteBuffers: ObjectPool<ByteBuffer>")
		}
		return code.identBracketsCurly(") : AbstractApiAdapter<$iaName, $oaName>(sluice, coroutineScope, byteBuffers) ")
	}
}
