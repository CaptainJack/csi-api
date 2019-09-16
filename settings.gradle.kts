rootProject.name = "csi-api"

include(
	":common",
	":client",
	":server"
)

arrayOf("common", "client", "server").forEach { project(":$it").name = "${rootProject.name}-$it" }

enableFeaturePreview("GRADLE_METADATA")
