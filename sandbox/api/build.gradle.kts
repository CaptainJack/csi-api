import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
	kotlin("multiplatform")
}

kotlin {
	jvm()
	js(IR) {
		browser()
	}
	
	emptySourceSets("common")
	emptySourceSets("jvm")
	emptySourceSets("js")
	
	sourceSets.getByName("commonMain") {
		dependencies {
			implementation(project(":runtime:csi-api-common"))
		}
		kotlin.setSrcDirs(listOf("src"))
	}
}

subprojects {
	pluginManager.apply("org.jetbrains.kotlin.multiplatform")
	
	configure<KotlinMultiplatformExtension> {
		jvm()
		
		emptySourceSets("common")
		emptySourceSets("jvm")
		
		sourceSets.getByName("commonMain") {
			dependencies {
				api(project(":sandbox:api"))
				api(project(":runtime:csi-api-${project.name}"))
			}
			kotlin.setSrcDirs(listOf("src"))
		}
		
		if (name == "client") {
			js(IR) {
				browser()
			}
		}
	}
}



fun KotlinMultiplatformExtension.emptySourceSets(name: String) {
	sourceSets.getByName(name + "Main") {
		kotlin.setSrcDirs(emptyList<String>())
		resources.setSrcDirs(emptyList<String>())
	}
	sourceSets.getByName(name + "Test") {
		kotlin.setSrcDirs(emptyList<String>())
		resources.setSrcDirs(emptyList<String>())
	}
}
