package ru.capjack.csi.api.gradle

import org.gradle.api.Project
import java.io.File

open class CsiApiExtension(private val project: Project) {
	
	private val _targets = mutableListOf<ApiTarget>()
	
	var modelSnapshotFile = project.file("model.yml")
	
	var sourcePackage = defineSourcePackage(project.file("src"), "")
	
	val targets: List<ApiTarget>
		get() = _targets
	
	private fun defineSourcePackage(file: File, pkg: String): String {
		val files = file.listFiles()!!
		if (files.size == 1) {
			files[0].also {
				if (it.isDirectory) {
					val p = (if (pkg.isEmpty()) "" else "$pkg.") + it.name
					return defineSourcePackage(it, p)
				}
			}
		}
		return pkg
	}
	
	fun target(target: ApiTarget) {
		if (_targets.add(target)) {
			if (target.platform.isKotlin()) {
				val sideProject = project.getTargetProject(target)
				
				when (target.platform) {
					ApiPlatform.KOTLIN_JS  -> {
						project.configureKotlinJs()
						sideProject.configureKotlinSide(target.side)
						sideProject.configureKotlinJs()
					}
					ApiPlatform.KOTLIN_JVM -> {
						project.configureKotlinJvm()
						sideProject.configureKotlinSide(target.side)
						sideProject.configureKotlinJvm()
					}
				}
			}
		}
	}
	
	private fun Project.configureKotlinSide(side: ApiSide) {
		pluginManager.apply("org.jetbrains.kotlin.multiplatform")
		
		configureKmp {
			emptySourceSets("common")
			sourceSets.getByName("commonMain") {
				dependencies {
					implementation(kotlin("stdlib-common"))
					api("ru.capjack.csi:csi-api-${side.name.toLowerCase()}")
					api(project(project.parent!!.path))
				}
				kotlin.setSrcDirs(listOf("src"))
			}
		}
	}
	
	private fun Project.configureKotlinJvm() {
		configureKmp {
			jvm {
				compilations.all { kotlinOptions.jvmTarget = "1.8" }
			}
			emptySourceSets("jvm")
			sourceSets.getByName("jvmMain") {
				dependencies {
					implementation(kotlin("stdlib-jdk8"))
				}
			}
		}
		
		
	}
	
	private fun Project.configureKotlinJs() {
		configureKmp {
			js()
			emptySourceSets("js")
			sourceSets.getByName("jsMain") {
				dependencies {
					implementation(kotlin("stdlib-js"))
				}
			}
		}
	}
	
}

enum class ApiSide {
	CLIENT,
	SERVER
}

enum class ApiPlatform {
	KOTLIN_JS,
	KOTLIN_JVM;
}

enum class ApiTarget(val side: ApiSide, val platform: ApiPlatform) {
	CLIENT_KOTLIN_JS(ApiSide.CLIENT, ApiPlatform.KOTLIN_JS),
	CLIENT_KOTLIN_JVM(ApiSide.CLIENT, ApiPlatform.KOTLIN_JVM),
	SERVER_KOTLIN_JVM(ApiSide.SERVER, ApiPlatform.KOTLIN_JVM)
}