package ru.capjack.csi.api.generator.langs.yaml

import ru.capjack.csi.api.generator.ApiVersion
import ru.capjack.tool.biser.generator.langs.yaml.YamlModel

class ApiYamlModel : YamlModel() {
	var version = ApiVersion(0, 0)
	
	val client: Api = Api()
	val server: Api = Api()
	val services: MutableList<Service> = mutableListOf()
	
	class Api {
		var lastServiceId: Int = 0
		val services: MutableList<Service> = mutableListOf()
		
		class Service(val id: Int, val name: String, val descriptor: String)
	}
	
	class Service(
		val name: String,
		val lastMethodId: Int,
		val methods: List<Method>
	) {
		
		class Method(val id: Int, val name: String, val suspend: Boolean, val arguments: List<Argument>, val result: String?) {
			class Argument(
				val name: String,
				val type: String?,
				val parameters: List<Parameter>?
			)
		}
	}
	
	class Parameter(val name: String?, val type: String)
}