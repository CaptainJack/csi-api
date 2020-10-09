import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

plugins {
	kotlin("multiplatform") version "1.4.10" apply false
	id("nebula.release") version "15.2.0"
	id("ru.capjack.depver") version "1.2.0"
	id("ru.capjack.bintray") version "1.0.0"
}

depver {
	"ru.capjack.tool" {
		"tool-lang"("1.5.0")
		"tool-utils"("0.15.0")
		"tool-logging"("1.2.0")
		"tool-io-biser*"("0.7.0")
	}
	"ru.capjack.csi" {
		"csi-core-*"("0.5.0-dev.3+011d4ed")
		"csi-transport-*"("0.3.0-dev.2.uncommitted+f1d5818")
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
