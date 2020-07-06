package ru.capjack.csi.api.gradle

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.findPlugin
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.getPlugin
import org.gradle.kotlin.dsl.typeOf
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.io.File

internal fun defineSourcePackage(file: File, pkg: String): String {
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

inline fun <R> Project.kmp(configuration: KotlinMultiplatformExtension.() -> R): R {
	return extensions.getByType<KotlinMultiplatformExtension>().run(configuration)
}

fun Project.kmpSourceDir(name: String): File = kmp {
	sourceSets.getByName(name).kotlin.srcDirs.first()
}

val Project.kmpSourceDirCommonMain: File get() = kmpSourceDir("commonMain")