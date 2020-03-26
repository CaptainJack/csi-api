plugins {
	kotlin("multiplatform")
	id("ru.capjack.bintray")
}

kotlin {
	jvm()
	
	sourceSets {
		get("commonMain").dependencies {
			implementation(kotlin("stdlib-common"))
			api(project(":runtime:csi-api-common"))
			api("ru.capjack.csi:csi-core-server")
		}
		
		get("jvmMain").dependencies {
			implementation(kotlin("stdlib-jdk8"))
		}
	}
}
