import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("kotlin-platform-jvm")
	id("ru.capjack.degos.publish")
}

dependencies {
	implementation(kotlin("stdlib-jdk8"))
	implementation(kotlin("reflect"))
	implementation("com.beust:klaxon:3.0.1")
	expectedBy(project(":csi-messenger:csi-messenger-common"))
}