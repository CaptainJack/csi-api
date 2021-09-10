plugins {
	kotlin("multiplatform") version "1.5.30" apply false
	id("ru.capjack.publisher") version "1.0.0"
	id("ru.capjack.depver") version "1.2.0"
}

depver {
	"ru.capjack.tool" {
		"tool-lang"("1.11.1")
		"tool-utils"("1.7.0")
		"tool-logging"("1.5.0")
		"tool-io"("1.0.0")
		"tool-biser"("1.1.0")
		"tool-biser-generator"("1.1.1")
	}
	"ru.capjack.csi" {
		"csi-core-*"("1.1.0")
		"csi-transport-*"("1.0.0")
	}
	"org.jetbrains.kotlinx:kotlinx-coroutines-core"("1.5.2")
}

allprojects {
	group = "ru.capjack.csi"
	
	repositories {
		mavenCentral()
		mavenCapjack()
		mavenLocal()
	}
	
	afterEvaluate {
		if (plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
			configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
				targets.forEach {
					if (it is org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget) {
						it.compilations.all { kotlinOptions.jvmTarget = "11" }
					}
				}
			}
		}
		else if (plugins.hasPlugin("org.jetbrains.kotlin.jvm")) {
			configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
				target.compilations.all { kotlinOptions.jvmTarget = "11" }
			}
		}
	}
}
