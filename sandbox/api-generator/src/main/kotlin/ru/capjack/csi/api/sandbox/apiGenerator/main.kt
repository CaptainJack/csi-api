package ru.capjack.csi.api.sandbox.apiGenerator

import ru.capjack.csi.api.generator.kotlin.ClientJsKotlinCsiApiGenerator
import ru.capjack.csi.api.generator.kotlin.ClientKotlinCsiApiGenerator
import ru.capjack.csi.api.generator.kotlin.KotlinCsiApiGenerator
import ru.capjack.csi.api.generator.kotlin.ServerKotlinCsiApiGenerator
import ru.capjack.csi.api.generator.model.KotlinApiModelDelegate
import ru.capjack.tool.biser.generator.CodePath
import java.nio.file.Paths

fun main() {
	val projectApiPath = Paths.get("sandbox/api")
	
	val delegate = KotlinApiModelDelegate(
		"ru.capjack.csi.api.sandbox.api",
		projectApiPath.resolve("src"),
		projectApiPath.resolve("model.yml")
	)
	
//	ClientJsKotlinCsiApiGenerator("csi").generate(delegate.model, projectApiPath.resolve("client-js/src"))
	ClientKotlinCsiApiGenerator(delegate.sourcePackage).generate(delegate.model, projectApiPath.resolve("client/src/commonMain/kotlin"))
//	ServerKotlinCsiApiGenerator(delegate.sourcePackage).generate(delegate.model, projectApiPath.resolve("server/src"))
}