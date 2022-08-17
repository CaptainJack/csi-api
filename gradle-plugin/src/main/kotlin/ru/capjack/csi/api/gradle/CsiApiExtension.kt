package ru.capjack.csi.api.gradle

import org.gradle.api.Project
import java.io.File
import java.nio.file.Paths

open class CsiApiExtension(private val project: Project) {
	
	private val _targets = mutableSetOf<ApiTarget>()
	
	var modelFile: File = project.file("src/model.yml")
	var sourcePackage = defineSourcePackage(project.kmpSourceDirCommonMain, "")
	
	val targets: Set<ApiTarget> get() = _targets
	
	fun target(target: ApiTarget) {
		if (_targets.add(target)) {
			target.configureProject(project)
		}
	}
	
	fun targetClientTypescriptSeparated(
		name: String = "client",
		targetSourceDir: String,
		genPrefix: String,
		genPath: String,
		libPath: String,
		cutGenPrefix: String? = null,
		cutLibPrefix: String? = null
	) {
		target(
			ClientTypescriptSeparatedApiTarget(
				name,
				targetSourceDir.let { if (it.startsWith("..")) project.projectDir.resolve(it).absoluteFile.toPath() else Paths.get(it) },
				genPrefix,
				genPath,
				libPath,
				cutGenPrefix,
				cutLibPrefix
			)
		)
	}
	
	fun targetClientKotlin(name: String = "client", vararg platforms: KotlinPlatform = arrayOf(KotlinPlatform.JS), jvm: String = "17") {
		target(ClientKotlinApiTarget(name, platforms.toSet(), jvm))
	}
	
	fun targetServerKotlin(name: String = "server", jvm: String = "17") {
		target(ServerKotlinApiTarget(name, jvm))
	}
}