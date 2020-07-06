package ru.capjack.csi.api.sandbox.apiGenerator

import ru.capjack.csi.api.generator.kotlin.ClientKotlinCsiApiGenerator
import ru.capjack.csi.api.generator.kotlin.KotlinCsiApiGenerator
import ru.capjack.csi.api.generator.kotlin.ServerKotlinCsiApiGenerator
import ru.capjack.csi.api.generator.model.KotlinApiModelDelegate
import ru.capjack.tool.io.biser.generator.CodePath
import java.nio.file.Paths

fun main() {
	val projectApiPath = Paths.get("sandbox/api")
	
	val delegate = KotlinApiModelDelegate(
		"ru.capjack.csi.api.sandbox.api",
		projectApiPath.resolve("src"),
		projectApiPath.resolve("model.yml")
	)
	
	ClientKotlinCsiApiGenerator(delegate.sourcePackage).generate(delegate.model, projectApiPath.resolve("client/src"))
	ServerKotlinCsiApiGenerator(delegate.sourcePackage).generate(delegate.model, projectApiPath.resolve("server/src"))
}