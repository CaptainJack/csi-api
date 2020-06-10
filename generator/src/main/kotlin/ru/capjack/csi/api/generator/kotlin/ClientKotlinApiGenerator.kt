package ru.capjack.csi.api.generator.kotlin

import ru.capjack.csi.api.generator.model.ApiModel
import ru.capjack.tool.io.biser.generator.CodeBlock
import ru.capjack.tool.io.biser.generator.CodePath
import ru.capjack.tool.io.biser.generator.kotlin.KotlinCodersGenerator
import java.nio.file.Path

class ClientKotlinApiGenerator(
	targetPackage: CodePath,
	coders: KotlinCodersGenerator
) : AbstractKotlinApiGenerator(coders, targetPackage, "client") {
	override fun generate(model: ApiModel, targetSrc: Path) {
		generate(model.client, model.server, targetSrc)
	}
	
	override fun generateApiAdapterDeclaration(code: CodeBlock, iaName: String, oaName: String): CodeBlock {
		code.line("class ApiAdapter(")
		code.ident {
			line("sluice: ApiSluice<$iaName, $oaName>,")
			line("byteBuffers: ObjectPool<ByteBuffer>")
		}
		return code.identBracketsCurly(") : AbstractApiAdapter<$iaName, $oaName>(sluice, byteBuffers) ")
	}
}
