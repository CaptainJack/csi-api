package ru.capjack.csi.api.generator.langs.typescript

import ru.capjack.csi.api.generator.model.ApiModel
import ru.capjack.tool.biser.generator.Code
import ru.capjack.tool.biser.generator.langs.typescript.TsCodeFile
import ru.capjack.tool.biser.generator.langs.typescript.TsCodersGenerator

open class ClientTsApiGenerator(
	model: ApiModel,
	coders: TsCodersGenerator,
	targetPackage: String
) : TsApiGenerator(model, coders, targetPackage, "client") {
	
	override fun generate(files: MutableList<TsCodeFile>) {
		generate(model.client, model.server, files)
	}
	
	override fun generateApiAdapterDeclaration(code: Code, iaName: String, oaName: String): Code {
		return code.identBracketsCurly("export class ApiAdapter extends AbstractApiAdapter<$iaName, $oaName> ").apply {
			line("constructor(")
			ident {
				line("sluice: ApiSluice<$iaName, $oaName>,")
				line("byteBuffers: ObjectPool<ByteBuffer>")
			}
			line(") {")
			ident() {
				line("super(sluice, byteBuffers)")
			}
			line("}")
		}
	}
}
