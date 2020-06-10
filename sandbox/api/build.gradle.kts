import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
	kotlin("multiplatform")
}

kotlin {
	jvm()
	js()
	
	emptySourceSets("common")
	emptySourceSets("jvm")
	emptySourceSets("js")
	
	sourceSets.getByName("commonMain") {
		dependencies {
			implementation(kotlin("stdlib-common"))
			implementation(project(":runtime:csi-api-common"))
		}
		kotlin.setSrcDirs(listOf("src"))
	}
	
	sourceSets.getByName("jvmMain").dependencies {
		implementation(kotlin("stdlib-jdk8"))
	}
	sourceSets.getByName("jsMain").dependencies {
		implementation(kotlin("stdlib-js"))
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
				implementation(kotlin("stdlib-common"))
				api(project(":sandbox:api"))
				api(project(":runtime:csi-api-${project.name}"))
			}
			kotlin.setSrcDirs(listOf("src"))
		}
		
		sourceSets.getByName("jvmMain").dependencies {
			implementation(kotlin("stdlib-jdk8"))
		}
		
		if (name == "client") {
			js()
			sourceSets.getByName("jsMain").dependencies {
				implementation(kotlin("stdlib-js"))
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
