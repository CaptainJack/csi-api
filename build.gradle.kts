import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.js.KotlinJsTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

plugins {
	kotlin("multiplatform") version "1.3.72" apply false
	id("nebula.release") version "15.1.0"
	id("ru.capjack.depver") version "1.1.0"
	id("ru.capjack.bintray") version "1.0.0"
	id("ru.capjack.logging") version "1.1.0"
}

depver {
	"ru.capjack.tool" {
		"tool-utils"("0.10.1")
		"tool-lang"("1.2.0")
		"tool-io-biser*"("0.4.2")
	}
	"ru.capjack.csi" {
		"csi-core-*"("0.4.0")
		"csi-transport-*"("0.2.0")
	}
}

allprojects {
	group = "ru.capjack.csi"
	
	repositories {
		jcenter()
		maven("https://dl.bintray.com/capjack/public")
		mavenLocal()
	}
	
	afterEvaluate {
		if (plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
			configure<KotlinMultiplatformExtension> {
				targets.forEach {
					if (it is KotlinJvmTarget) {
						it.compilations.all { kotlinOptions.jvmTarget = "1.8" }
					}
					else if (it is KotlinJsTarget) {
						it.compilations.all { kotlinOptions.sourceMap = false }
					}
				}
			}
		}
		else if (plugins.hasPlugin("org.jetbrains.kotlin.jvm")) {
			configure<KotlinJvmProjectExtension> {
				target.compilations.all { kotlinOptions.jvmTarget = "1.8" }
			}
		}
	}
}
