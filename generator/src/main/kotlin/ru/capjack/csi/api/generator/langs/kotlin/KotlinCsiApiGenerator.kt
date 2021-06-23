package ru.capjack.csi.api.generator.langs.kotlin

import ru.capjack.csi.api.generator.CsiApiGenerator
import ru.capjack.csi.api.generator.model.ApiModel
import ru.capjack.tool.biser.generator.langs.kotlin.KotlinCodersGenerator
import java.nio.file.Path

abstract class KotlinCsiApiGenerator(protected val sourcePackage: String) : CsiApiGenerator {
	override fun generate(model: ApiModel, targetSourceDir: Path) {
		val codersGenerator = createCodersGenerator(model)
		createApiGenerator(model, codersGenerator).generate(targetSourceDir)
		codersGenerator.generate(targetSourceDir)
	}
	
	protected abstract fun createApiGenerator(model: ApiModel, codersGenerator: KotlinCodersGenerator): KotlinApiGenerator
	
	protected open fun createCodersGenerator(model: ApiModel): KotlinCodersGenerator {
		return KotlinCodersGenerator(model, sourcePackage, internal = true)
	}
}

