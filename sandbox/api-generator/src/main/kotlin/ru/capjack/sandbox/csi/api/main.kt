package ru.capjack.sandbox.csi.api

import ru.capjack.csi.api.generator.langs.kotlin.ClientKotlinCsiApiGenerator
import ru.capjack.csi.api.generator.langs.kotlin.ServerKotlinCsiApiGenerator
import ru.capjack.csi.api.generator.langs.typescript.ClientTsCsiApiGenerator
import ru.capjack.csi.api.generator.model.KotlinApiModelDelegate
import java.nio.file.Paths

fun main() {
	val projectApiPath = Paths.get("sandbox/api")
	
	val delegate = KotlinApiModelDelegate(
		"ru.capjack.sandbox.csi.api",
		projectApiPath.resolve("src"),
		projectApiPath.resolve("model.yml")
	)

	ClientKotlinCsiApiGenerator(delegate.sourcePackage).generate(delegate.model, projectApiPath.resolve("client/src/commonMain/kotlin"))
	ServerKotlinCsiApiGenerator(delegate.sourcePackage).generate(delegate.model, projectApiPath.resolve("server/src/commonMain/kotlin"))
	
	ClientTsCsiApiGenerator(delegate.sourcePackage).generate(delegate.model, Paths.get("/Users/shnyaka/workspace/projects/capjack/app/CaptainJackSlots/app-CaptainJackSlots-client-new/assets/lib/internal"))
}