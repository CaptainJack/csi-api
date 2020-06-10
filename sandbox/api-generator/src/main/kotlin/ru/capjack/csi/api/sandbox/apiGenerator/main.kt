package ru.capjack.csi.api.sandbox.apiGenerator

import ru.capjack.csi.api.generator.CsiApiGenerator
import java.nio.file.Path
import java.nio.file.Paths

fun main() {
	val projectApiPath = Paths.get("sandbox/api")
	
	val generator = CsiApiGenerator("ru.capjack.csi.api.sandbox.api")
	
	val modelSnapshotFile = projectApiPath.resolve("model.yml").toFile()
	
	if (modelSnapshotFile.exists()) {
		generator.model.load(modelSnapshotFile.readText())
	}
	
	generator.loadKotlin(projectApiPath.resolve("src"))
	
	generator.generateKotlinClient(projectApiPath.resolve("client/src"))
	generator.generateKotlinServer(projectApiPath.resolve("server/src"))
	
	modelSnapshotFile.writeText(generator.model.save())
}