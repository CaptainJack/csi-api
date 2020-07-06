package ru.capjack.csi.api.gradle

import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.io.File

open class CsiApiExtension(private val project: Project) {
	
	private val _targets = mutableSetOf<ApiTarget>()
	
	var modelSnapshotFile: File = project.file("model.yml")
	var sourcePackage = defineSourcePackage(project.kmpSourceDirCommonMain, "")
	
	val targets: Set<ApiTarget> get() = _targets
	
	fun target(target: ApiTarget) {
		if (_targets.add(target)) {
			target.configureProject(project)
		}
	}
	
	fun targetKotlinClient(name: String = "client", vararg platforms: KotlinPlatform = arrayOf(KotlinPlatform.JS)) {
		target(ClientKotlinApiTarget(name, platforms.toSet()))
	}
	
	fun targetKotlinServer(name: String = "server") {
		target(ServerKotlinApiTarget(name))
	}
}