package ru.capjack.csi.api.generator

import ru.capjack.csi.api.generator.kotlin.ApiKotlinModelLoader
import ru.capjack.csi.api.generator.kotlin.ClientKotlinApiGenerator
import ru.capjack.csi.api.generator.kotlin.ServerKotlinApiGenerator
import ru.capjack.csi.api.generator.model.ApiModel
import ru.capjack.tool.io.biser.generator.CodePath
import ru.capjack.tool.io.biser.generator.kotlin.KotlinCodersGenerator
import ru.capjack.tool.io.biser.generator.kotlin.KotlinSource
import java.nio.file.Path

class CsiApiGenerator(sourcePackage: String) {
	private val model = ApiModel()
	private val sourcePackage = CodePath(sourcePackage)
	
	fun loadKotlin(sourcePath: Path) {
		val source = KotlinSource(sourcePath)
		ApiKotlinModelLoader(model, source, sourcePackage.value).load()
	}
	
	fun generateKotlinClient(targetSourcePath: Path) {
		val codersGenerator = KotlinCodersGenerator(sourcePackage, true)
		ClientKotlinApiGenerator(sourcePackage, codersGenerator).generate(model, targetSourcePath)
		
		codersGenerator.generate(targetSourcePath)
	}
	
	fun generateKotlinServer(targetSourcePath: Path) {
		val codersGenerator = KotlinCodersGenerator(sourcePackage, true)
		ServerKotlinApiGenerator(sourcePackage, codersGenerator).generate(model, targetSourcePath)
		
		codersGenerator.generate(targetSourcePath)
	}
}