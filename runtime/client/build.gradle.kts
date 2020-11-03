plugins {
	kotlin("multiplatform")
	id("ru.capjack.bintray")
}

kotlin {
	jvm()
	js(IR) {
		browser()
	}
	
	sourceSets {
		get("commonMain").dependencies {
			api(project(":runtime:csi-api-common"))
			api("ru.capjack.csi:csi-core-client")
		}
		get("jsMain").dependencies {
			implementation("ru.capjack.csi:csi-transport-js-client-browser")
		}
	}
}
