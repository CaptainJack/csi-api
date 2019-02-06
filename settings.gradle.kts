
rootProject.name = "csi"
include("kotlin")

listOf("server", "common", "client").forEach {
	val p = ":kotlin/$it"
	include(p)
	project(p).name = "kotlin-$it"
}