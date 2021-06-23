plugins {
	kotlin("multiplatform")
	id("ru.capjack.publisher")
}

kotlin {
	jvm()
	js(IR) {
		browser()
	}
	
	sourceSets {
		get("commonMain").dependencies {
			implementation("ru.capjack.tool:tool-lang")
			api("ru.capjack.csi:csi-core-common")
			api("ru.capjack.tool:tool-biser")
			api("ru.capjack.tool:tool-logging")
			api("org.jetbrains.kotlinx:kotlinx-coroutines-core")
		}
		get("jvmMain").dependencies {
			implementation(kotlin("reflect"))
		}
	}
}
