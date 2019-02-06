import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation.Companion.MAIN_COMPILATION_NAME
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation.Companion.TEST_COMPILATION_NAME

plugins {
	kotlin("multiplatform") version "1.3.20"
}

repositories {
	mavenCentral()
}

kotlin {
	jvm {
		compilations.all {
			kotlinOptions {
				jvmTarget = "1.8"
			}
		}
	}
	sourceSets {
		commonMain {
			dependencies {
				implementation(kotlin("stdlib-common"))
			}
		}
		
		jvm().compilations[MAIN_COMPILATION_NAME].defaultSourceSet {
			dependencies {
				implementation(kotlin("stdlib-jdk8"))
			}
		}
		
		jvm().compilations[TEST_COMPILATION_NAME].defaultSourceSet {
			dependencies {
				implementation(kotlin("test-junit"))
			}
		}
	}
}