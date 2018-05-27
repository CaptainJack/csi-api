plugins {
	id("kotlin")
	id("ru.capjack.degos.publish")
}

dependencies {
	implementation(kotlin("stdlib-jdk8"))
	implementation(project(":csi-core:csi-core-jvm"))
}