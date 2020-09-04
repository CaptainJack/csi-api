package ru.capjack.csi.api.gradle

import ru.capjack.csi.api.generator.JsLegacyCsiApiGenerator
import ru.capjack.csi.api.generator.model.KotlinApiModelDelegate

class JsLegacyApiTarget(name: String) : ApiTarget(name) {
	override fun generate(delegate: KotlinApiModelDelegate) {
		JsLegacyCsiApiGenerator().generate(delegate.model, project.projectDir.toPath())
	}
}