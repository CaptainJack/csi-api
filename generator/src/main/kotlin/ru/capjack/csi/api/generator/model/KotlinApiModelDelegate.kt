package ru.capjack.csi.api.generator.model

import ru.capjack.csi.api.generator.kotlin.ApiKotlinModelLoader
import ru.capjack.tool.io.biser.generator.CodePath
import ru.capjack.tool.io.biser.generator.kotlin.KotlinSource
import java.nio.file.Files
import java.nio.file.Path

class KotlinApiModelDelegate(sourcePackage: String, sourcePath: Path, snapshot: Path) {
	val model = ApiModel()
	val sourcePackage = CodePath(sourcePackage)
	
	init {
		loadSnapshot(snapshot)
		loadKotlin(sourcePath)
		saveSnapshot(snapshot)
	}
	
	private fun loadSnapshot(path: Path) {
		if (Files.exists(path)) {
			val text = Files.newBufferedReader(path).use { it.readText() }
			if (text.isNotBlank()) {
				model.load(text)
			}
		}
	}
	
	private fun saveSnapshot(path: Path) {
		Files.newBufferedWriter(path).use { it.write(model.save()) }
	}
	
	private fun loadKotlin(sourcePath: Path) {
		val source = KotlinSource(sourcePath)
		ApiKotlinModelLoader(model, source, sourcePackage.value).load()
	}
}
