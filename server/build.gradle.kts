plugins {
	kotlin("multiplatform")
	id("ru.capjack.bintray")
}

kotlin {
	jvm()
	
	sourceSets {
		get("commonMain").dependencies {
			implementation(kotlin("stdlib-common"))
			implementation("ru.capjack.csi:csi-transport-server")
		}
		
		get("jvmMain").dependencies {
			implementation(kotlin("stdlib-jdk8"))
		}
	}
}
