import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
	kotlin("multiplatform")
}

kotlin {
	jvm()
	
	sourceSets["commonMain"].dependencies {
		api(project(":runtime:csi-api-common"))
		api("ru.capjack.tool:tool-utils")
	}
}

subprojects {
	pluginManager.apply("org.jetbrains.kotlin.multiplatform")
	
	configure<KotlinMultiplatformExtension> {
		sourceSets["commonMain"].dependencies {
			api(project(":sandbox:api"))
			when (name) {
				"client" -> kotlin {
					api(project(":runtime:csi-api-client"))
				}
				"server" -> kotlin {
					api(project(":runtime:csi-api-server"))
				}
			}
		}
	}
	
	when (name) {
		"client" -> kotlin {
			jvm()
		}
		"server" -> kotlin {
			jvm()
		}
	}
}
