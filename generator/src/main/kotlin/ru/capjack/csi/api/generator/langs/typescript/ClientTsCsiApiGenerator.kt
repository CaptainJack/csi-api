package ru.capjack.csi.api.generator.langs.typescript

import ru.capjack.csi.api.generator.CsiApiGenerator
import ru.capjack.csi.api.generator.model.ApiModel
import ru.capjack.tool.biser.generator.langs.typescript.TsCodersGenerator
import java.nio.file.Path

class ClientTsCsiApiGenerator(private val sourcePackage: String) : CsiApiGenerator {
	override fun generate(model: ApiModel, targetSourceDir: Path) {
		val codersGenerator = TsCodersGenerator(model, "$sourcePackage._impl")
		ClientTsApiGenerator(model, codersGenerator, sourcePackage).generate(targetSourceDir)
		codersGenerator.generate(targetSourceDir)
	}
}