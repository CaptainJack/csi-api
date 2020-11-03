package ru.capjack.csi.api.gradle

import org.gradle.api.Project
import java.io.File

open class CsiApiExtension(private val project: Project) {
	
	private val _targets = mutableSetOf<ApiTarget>()
	
	var modelSnapshotFile: File = project.file("src/model.yml")
	var sourcePackage = defineSourcePackage(project.kmpSourceDirCommonMain, "")
	
	val targets: Set<ApiTarget> get() = _targets
	
	fun target(target: ApiTarget) {
		if (_targets.add(target)) {
			target.configureProject(project)
		}
	}
	
	fun targetClientJsKotlin(name: String = "client", module: String = "csi") {
		target(ClientJsKotlinApiTarget(name, module))
	}
	
	fun targetClientKotlin(name: String = "client", vararg platforms: KotlinPlatform = arrayOf(KotlinPlatform.JS)) {
		target(ClientKotlinApiTarget(name, platforms.toSet()))
	}
	
	fun targetServerKotlin(name: String = "server") {
		target(ServerKotlinApiTarget(name))
	}
}