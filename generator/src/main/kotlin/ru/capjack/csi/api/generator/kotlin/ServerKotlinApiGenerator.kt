package ru.capjack.csi.api.generator.kotlin

import ru.capjack.csi.api.generator.model.ApiModel
import ru.capjack.tool.io.biser.generator.CodeBlock
import ru.capjack.tool.io.biser.generator.CodePath
import ru.capjack.tool.io.biser.generator.kotlin.KotlinCodersGenerator
import java.nio.file.Path

class ServerKotlinApiGenerator(
	targetPackage: CodePath,
	coders: KotlinCodersGenerator
) : AbstractKotlinApiGenerator(coders, targetPackage, "server") {
	
	override fun generate(model: ApiModel, targetSrc: Path) {
		generate(model.server, model.client, targetSrc)
	}
	
	override fun generateApiAdapterDeclaration(code: CodeBlock, iaName: String, oaName: String): CodeBlock {
		code.line("class ApiAdapter<I : Any>(")
		code.ident {
			line("sluice: ApiSluice<I, $iaName, $oaName>,")
			line("byteBuffers: ObjectPool<ByteBuffer>")
		}
		return code.identBracketsCurly(") : AbstractApiAdapter<I, $iaName, $oaName>(sluice, byteBuffers) ")
	}
}