rootProject.name = "csi-api"

include(
	"runtime:common",
	"runtime:client",
	"runtime:server",
	
	"generator",
	"gradle-plugin",
	
	"sandbox:api",
	"sandbox:api:client",
	"sandbox:api:server",
	"sandbox:api-generator",
	"sandbox:server-jvm",
	"sandbox:client-jvm"
)

arrayOf("common", "client", "server").forEach {
	project(":runtime:$it").name = "${rootProject.name}-$it"
}

project(":generator").name = "${rootProject.name}-generator"
project(":gradle-plugin").name = "${rootProject.name}-gradle"
