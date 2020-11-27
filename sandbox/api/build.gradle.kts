import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
	kotlin("multiplatform")
}

kotlin {
	jvm()
	js(IR) {
		browser()
	}
	
	sourceSets["commonMain"].dependencies {
		implementation(project(":runtime:csi-api-common"))
	}
}

subprojects {
	pluginManager.apply("org.jetbrains.kotlin.multiplatform")
	
	configure<KotlinMultiplatformExtension> {
		sourceSets["commonMain"].dependencies {
			api(project(":sandbox:api"))
		}
	}
}
