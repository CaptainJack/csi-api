import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

plugins {
	kotlin("multiplatform") version "1.4.20" apply false
	id("nebula.release") version "15.3.0"
	id("ru.capjack.depver") version "1.2.0"
	id("ru.capjack.bintray") version "1.0.0"
}

depver {
	"ru.capjack.tool" {
		"tool-lang"("1.7.0")
		"tool-utils"("1.1.1")
		"tool-logging"("1.2.2")
		"tool-biser*"("0.8.0")
	}
	"ru.capjack.csi" {
		"csi-core-*"("0.6.0")
		"csi-transport-*"("0.4.0")
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
