import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
	id("kotlin-platform-js")
	id("ru.capjack.degos-publish")
}

dependencies {
	implementation(kotlin("stdlib-js"))
	implementation("ru.capjack.ktjs:ktjs-common:0.2.+")
	expectedBy(project(":csi-messenger:csi-messenger-common"))
}

tasks.withType<Kotlin2JsCompile> {
	kotlinOptions {
		moduleKind = "amd"
	}
}