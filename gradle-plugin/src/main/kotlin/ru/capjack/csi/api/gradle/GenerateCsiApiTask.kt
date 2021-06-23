package ru.capjack.csi.api.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.getByType
import ru.capjack.csi.api.generator.model.KotlinApiModelDelegate

open class GenerateCsiApiTask : DefaultTask() {
	@TaskAction
	fun execute() {
		val extension = project.extensions.getByType<CsiApiExtension>()
		val apiModelDelegate = KotlinApiModelDelegate(
			extension.sourcePackage,
			project.kmpSourceDirCommonMain.toPath(),
			extension.modelFile.toPath()
		)
		
		extension.targets.forEach { it.generate(apiModelDelegate) }
	}
	
}

