package ru.capjack.csi.api.gradle

import org.gradle.api.internal.AbstractTask
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.getByType
import ru.capjack.csi.api.generator.CsiApiGenerator

open class GenerateCsiApiTask : AbstractTask() {
	@TaskAction
	fun execute() {
		val extension = project.extensions.getByType<CsiApiExtension>()
		val generator = CsiApiGenerator(extension.sourcePackage)
		
		if (extension.modelSnapshotFile.exists()) {
			generator.model.load(
				extension.modelSnapshotFile.readText()
			)
		}
		
		generator.loadKotlin(project.file("src").toPath())
		
		extension.modelSnapshotFile.writeText(
			generator.model.save()
		)
		
		extension.targets.filter { it.platform.isKotlin() }.map { it.side }.distinct().forEach { it ->
			when (it) {
				ApiSide.CLIENT -> generator.generateKotlinClient(project.project(project.path + ":client").file("src").toPath())
				ApiSide.SERVER -> generator.generateKotlinServer(project.project(project.path + ":server").file("src").toPath())
			}
		}
	}
	
}

