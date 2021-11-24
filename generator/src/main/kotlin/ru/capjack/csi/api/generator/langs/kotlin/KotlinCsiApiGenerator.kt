package ru.capjack.csi.api.generator.langs.kotlin

import ru.capjack.csi.api.generator.model.ApiModel
import ru.capjack.tool.biser.generator.langs.kotlin.KotlinCodeSource
import ru.capjack.tool.biser.generator.langs.kotlin.KotlinCodersGenerator
import java.nio.file.Path

abstract class KotlinCsiApiGenerator(protected val sourcePackage: String) {
	fun generate(model: ApiModel, targetSourceDir: Path) {
		val codersGenerator = createCodersGenerator(model)
		val apiGenerator = createApiGenerator(model, codersGenerator)
		
		val target = targetSourceDir.resolve(apiGenerator.targetPackage.full.joinToString("/")).toFile()
		
		val codeSource = KotlinCodeSource(targetSourceDir)
		
		apiGenerator.generate(codeSource)
		codersGenerator.generate(codeSource)
		
		val files = codeSource.saveNewFiles()
		
		target.walkBottomUp().forEach {
			if (it.isDirectory) {
				if (it.listFiles()?.size == 0) it.delete()
			} else if (!files.contains(it.toPath())) {
				it.delete()
			}
		}
	}
	
	protected abstract fun createApiGenerator(model: ApiModel, codersGenerator: KotlinCodersGenerator): KotlinApiGenerator
	
	protected open fun createCodersGenerator(model: ApiModel): KotlinCodersGenerator {
		return KotlinCodersGenerator(model, sourcePackage, internal = true)
	}
}

