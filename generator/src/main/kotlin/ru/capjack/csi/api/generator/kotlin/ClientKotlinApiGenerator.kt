package ru.capjack.csi.api.generator.kotlin

import ru.capjack.csi.api.generator.model.ApiModel
import ru.capjack.tool.biser.generator.CodeBlock
import ru.capjack.tool.biser.generator.CodeFile
import ru.capjack.tool.biser.generator.CodePath
import ru.capjack.tool.biser.generator.kotlin.KotlinCodersGenerator
import java.nio.file.Path

open class ClientKotlinApiGenerator(
	targetPackage: CodePath,
	coders: KotlinCodersGenerator
) : KotlinApiGenerator(coders, targetPackage, "client") {
	override fun generate(model: ApiModel, files: MutableList<CodeFile>) {
		generate(model.client, model.server, files)
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
