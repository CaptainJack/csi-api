plugins {
	kotlin("multiplatform")
	id("ru.capjack.bintray")
}


kotlin {
	jvm()
	js()
	
	sourceSets {
		get("commonMain").dependencies {
			implementation(kotlin("stdlib-common"))
			api("ru.capjack.csi:csi-core-common")
			api("ru.capjack.tool:tool-io-biser")
		}
		
		get("jvmMain").dependencies {
			implementation(kotlin("stdlib-jdk8"))
		}
		
		get("jsMain").dependencies {
			implementation(kotlin("stdlib-js"))
		}
	}
}
