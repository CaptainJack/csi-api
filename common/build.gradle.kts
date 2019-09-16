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
		}
		
		get("jvmMain").dependencies {
			implementation(kotlin("stdlib-jdk8"))
		}
		
		get("jsMain").dependencies {
			implementation(kotlin("stdlib-js"))
		}
	}
}
