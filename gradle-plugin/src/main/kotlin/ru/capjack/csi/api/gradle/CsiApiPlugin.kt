package ru.capjack.csi.api.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

open class CsiApiPlugin : Plugin<Project> {
	override fun apply(project: Project) {
		project.pluginManager.apply("org.jetbrains.kotlin.multiplatform")
		
		project.configureKmp {
			emptySourceSets("common")
			sourceSets.getByName("commonMain") {
				dependencies {
					implementation(kotlin("stdlib-common"))
				}
				kotlin.setSrcDirs(listOf("src"))
			}
		}
		
		project.extensions.create<CsiApiExtension>("csiApi", project)
		
		project.tasks.create<GenerateCsiApiTask>("generateCsiApi") {
			group = "build"
		}
	}
}


