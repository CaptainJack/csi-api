package ru.capjack.csi.api.generator.langs.kotlin

import ru.capjack.csi.api.generator.model.ApiModel
import ru.capjack.tool.biser.generator.Code
import ru.capjack.tool.biser.generator.langs.kotlin.KotlinCodeFile
import ru.capjack.tool.biser.generator.langs.kotlin.KotlinCodersGenerator

class ServerKotlinApiGenerator(
	model: ApiModel,
	coders: KotlinCodersGenerator,
	targetPackage: String
) : KotlinApiGenerator(model, coders, targetPackage, "server") {
	
	override fun generate(files: MutableList<KotlinCodeFile>) {
		generate(model.server, model.client, files)
	}
	
	override fun generateApiAdapterDeclaration(code: Code, iaName: String, oaName: String): Code {
		code.line("class ApiAdapter<I : Any>(")
		code.ident {
			line("sluice: ApiSluice<I, $iaName, $oaName>,")
			line("coroutineScope: CoroutineScope,")
			line("byteBuffers: ObjectPool<ByteBuffer>")
		}
		return code.identBracketsCurly(") : AbstractApiAdapter<I, $iaName, $oaName>(sluice, coroutineScope, byteBuffers) ")
	}
}