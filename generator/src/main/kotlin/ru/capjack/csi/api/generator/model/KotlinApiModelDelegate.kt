package ru.capjack.csi.api.generator.model

import ru.capjack.csi.api.generator.langs.kotlin.ApiKotlinModelLoader
import ru.capjack.csi.api.generator.langs.yaml.ApiYamlSnapshoter
import ru.capjack.tool.biser.generator.langs.kotlin.CommonKotlinSource
import ru.capjack.tool.biser.generator.langs.kotlin.KotlinPackageFilter
import ru.capjack.tool.biser.generator.langs.yaml.YamlModel
import java.nio.file.Files
import java.nio.file.Path

class KotlinApiModelDelegate(val sourcePackage: String, sourcePath: Path, snapshot: Path) {
	val model = ApiModel()
	private val snapshoter = ApiYamlSnapshoter()
	
	init {
		loadSnapshot(snapshot)
		loadKotlin(sourcePath)
		saveSnapshot(snapshot)
	}
	
	private fun loadSnapshot(path: Path) {
		if (Files.exists(path)) {
			val text = Files.newBufferedReader(path).use { it.readText() }
			if (text.isNotBlank()) {
				snapshoter.load(model, path.toFile())
			}
		}
	}
	
	private fun saveSnapshot(path: Path) {
		snapshoter.save(model, path.toFile())
	}
	
	private fun loadKotlin(sourcePath: Path) {
		val source = CommonKotlinSource(sourcePath)
		ApiKotlinModelLoader(source, model).load(KotlinPackageFilter(sourcePackage))
	}
}
