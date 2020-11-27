package ru.capjack.csi.api.generator.kotlin

import ru.capjack.csi.api.generator.CsiApiGenerator
import ru.capjack.csi.api.generator.model.ApiModel
import ru.capjack.tool.biser.generator.CodePath
import ru.capjack.tool.biser.generator.kotlin.KotlinCodersGenerator
import java.nio.file.Path

abstract class KotlinCsiApiGenerator(protected val sourcePackage: CodePath) : CsiApiGenerator {
	override fun generate(model: ApiModel, path: Path) {
		val codersGenerator = createCodersGenerator()
		createApiGenerator(sourcePackage, codersGenerator).generate(model, path)
		codersGenerator.generate(path)
	}
	
	protected abstract fun createApiGenerator(sourcePackage: CodePath, codersGenerator: KotlinCodersGenerator): KotlinApiGenerator
	
	protected open fun createCodersGenerator(): KotlinCodersGenerator {
		return KotlinCodersGenerator(sourcePackage, true)
	}
}

