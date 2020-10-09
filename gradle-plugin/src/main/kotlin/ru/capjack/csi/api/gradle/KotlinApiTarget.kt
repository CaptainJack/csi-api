package ru.capjack.csi.api.gradle

import org.gradle.api.Project
import ru.capjack.csi.api.generator.kotlin.ClientKotlinCsiApiGenerator
import ru.capjack.csi.api.generator.kotlin.ServerKotlinCsiApiGenerator
import ru.capjack.csi.api.generator.model.KotlinApiModelDelegate

abstract class KotlinApiTarget(
	name: String,
	private val side: ApiSide,
	private val platforms: Set<KotlinPlatform>
) : ApiTarget(name) {
	override fun configureProject(sourceProject: Project) {
		super.configureProject(sourceProject)
		project.pluginManager.apply("org.jetbrains.kotlin.multiplatform")
		
		platforms.forEach {
			when (it) {
				KotlinPlatform.JVM -> {
					sourceProject.configureJvm()
					project.configureJvm()
				}
				KotlinPlatform.JS  -> {
					sourceProject.configureJs()
					project.configureJs()
				}
			}
		}
		
		project.configureSide(side)
	}
	
	private fun Project.configureJvm() {
		kmp {
			jvm {
				compilations.all { kotlinOptions.jvmTarget = "1.8" }
			}
		}
	}
	
	private fun Project.configureJs() {
		kmp {
			js(IR) {
				browser()
			}
		}
	}
	
	private fun Project.configureSide(side: ApiSide) {
		kmp {
			sourceSets.getByName("commonMain") {
				dependencies {
					api("ru.capjack.csi:csi-api-${side.name.toLowerCase()}")
					api(project(project.parent!!.path))
				}
			}
		}
	}
}


class ServerKotlinApiTarget(name: String) : KotlinApiTarget(name, ApiSide.SERVER, setOf(KotlinPlatform.JVM)) {
	override fun generate(delegate: KotlinApiModelDelegate) {
		ServerKotlinCsiApiGenerator(delegate.sourcePackage).generate(delegate.model, project.kmpSourceDirCommonMain.toPath())
	}
}

class ClientKotlinApiTarget(name: String, platforms: Set<KotlinPlatform>) : KotlinApiTarget(name, ApiSide.CLIENT, platforms) {
	override fun generate(delegate: KotlinApiModelDelegate) {
		ClientKotlinCsiApiGenerator(delegate.sourcePackage).generate(delegate.model, project.kmpSourceDirCommonMain.toPath())
	}
}