package ru.capjack.csi.api.gradle

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun ApiPlatform.isKotlin(): Boolean {
	return this == ApiPlatform.KOTLIN_JS || this == ApiPlatform.KOTLIN_JVM
}

internal fun Project.getTargetProject(target: ApiTarget): Project {
	return project(path + ":" + target.side.name.toLowerCase())
}

internal fun Project.configureKmp(configuration: KotlinMultiplatformExtension.() -> Unit) {
//	configure(configuration)
}


internal fun KotlinMultiplatformExtension.emptySourceSets(name: String) {
	/*sourceSets.getByName(name + "Main") {
		kotlin.setSrcDirs(emptyList<String>())
		resources.setSrcDirs(emptyList<String>())
	}
	sourceSets.getByName(name + "Test") {
		kotlin.setSrcDirs(emptyList<String>())
		resources.setSrcDirs(emptyList<String>())
	}*/
}
