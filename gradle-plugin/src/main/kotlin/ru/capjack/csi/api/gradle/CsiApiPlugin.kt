package ru.capjack.csi.api.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create

open class CsiApiPlugin : Plugin<Project> {
	override fun apply(project: Project) {
		project.apply(plugin = "org.jetbrains.kotlin.multiplatform")
		
		project.kmp {
			jvm()
			sourceSets.getByName("commonMain") {
				dependencies {
					implementation("ru.capjack.csi:csi-api-common")
				}
			}
		}
		
		project.extensions.create<CsiApiExtension>("csiApi", project)
		
		project.tasks.create<GenerateCsiApiTask>("generateCsiApi") {
			group = "build"
		}
	}
}


