plugins {
	`kotlin-dsl`
	`java-gradle-plugin`
	`maven-publish`
	id("com.gradle.plugin-publish") version "0.10.1"
	id("ru.capjack.bintray")
}

dependencies {
	implementation(kotlin("stdlib-jdk8"))
	implementation(kotlin("gradle-plugin"))
	implementation(project(":csi-api-generator"))
}

gradlePlugin {
	plugins.create("CsiApi") {
		id = "ru.capjack.csi.api"
		implementationClass = "ru.capjack.csi.api.gradle.CsiApiPlugin"
		displayName = "ru.capjack.csi.api"
	}
}

pluginBundle {
	vcsUrl = "https://github.com/CaptainJack/tool-csi-api"
	website = vcsUrl
	description = "Plugin for support CSI API"
	tags = listOf("capjack", "csi")
}

rootProject.tasks["postRelease"].dependsOn(tasks["publishPlugins"])
