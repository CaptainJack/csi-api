package ru.capjack.csi.api.generator.langs.kotlin

import ru.capjack.csi.api.generator.model.ApiModel
import ru.capjack.tool.biser.generator.langs.kotlin.KotlinCodersGenerator

class ServerKotlinCsiApiGenerator(sourcePackage: String) : KotlinCsiApiGenerator(sourcePackage) {
	override fun createApiGenerator(model: ApiModel, codersGenerator: KotlinCodersGenerator): KotlinApiGenerator {
		return ServerKotlinApiGenerator(model, codersGenerator, sourcePackage)
	}
}