plugins {
	kotlin("multiplatform")
	id("ru.capjack.publisher")
}

kotlin {
	jvm()
	
	sourceSets {
		get("commonMain").dependencies {
			api(project(":runtime:csi-api-common"))
			api("ru.capjack.csi:csi-core-server")
		}
	}
}
