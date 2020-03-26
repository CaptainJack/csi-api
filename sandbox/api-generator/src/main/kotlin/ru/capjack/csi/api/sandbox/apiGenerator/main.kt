package ru.capjack.csi.api.sandbox.apiGenerator

import ru.capjack.csi.api.generator.CsiApiGenerator
import java.nio.file.Path
import java.nio.file.Paths

fun main() {
	val generator = CsiApiGenerator("ru.capjack.csi.api.sandbox.api")
	
	val projectApiPath = Paths.get("sandbox/api")
	
	generator.loadKotlin(projectApiPath.resolve("src"))
	
	generator.generateKotlinClient(projectApiPath.resolve("client/src"))
	generator.generateKotlinServer(projectApiPath.resolve("server/src"))
	
	
}