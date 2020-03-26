package ru.capjack.csi.api.generator.model

import ru.capjack.tool.io.biser.generator.CodePath
import ru.capjack.tool.io.biser.generator.model.Model
import ru.capjack.tool.utils.collections.getOrAdd
import ru.capjack.tool.utils.collections.mutableKeyedSetOf

class ApiModel : Model() {
	private val _client = ApiImpl(CodePath("client.ClientApi"))
	private val _server = ApiImpl(CodePath("server.ServerApi"))
	
	val client: Api get() = _client
	val server: Api get() = _server
	
	private val services = mutableKeyedSetOf<CodePath, ServiceDescriptorImpl> { it.path }
	
	fun provideServicesDescriptor(name: String): ServiceDescriptor {
		return provideServicesDescriptorImpl(name)
	}
	
	private fun provideServicesDescriptorImpl(path: String): ServiceDescriptorImpl {
		return services.getOrAdd(CodePath(path), ::ServiceDescriptorImpl)
	}
	
	@Suppress("UNCHECKED_CAST")
	override fun load(data: Map<String, Any>) {
		super.load(data)
		
		loadApi(_client, data["client"] as Map<String, Map<String, Any>>)
		loadApi(_server, data["server"] as Map<String, Map<String, Any>>)
		
		data["services"].asObjectList().mapTo(services) { s ->
			provideServicesDescriptorImpl(s["name"] as String).also { d ->
				d.lastMethodId = s["lastMethodId"] as Int
				s["methods"].asObjectList().mapTo(d.methods) { m ->
					MethodImpl(
						m["id"] as Int,
						m["name"] as String,
						m["arguments"].asObjectList().map {
							Parameter(it["name"] as String, loadType(it["type"] as String))
						},
						(m["result"] as List<Map<String, Any>>?)?.map {
							Parameter(it["name"] as String?, loadType(it["type"] as String))
						}
					)
				}
			}
		}
	}
	
	override fun save(data: MutableMap<String, Any>) {
		super.save(data)
		
		data["client"] = saveApi(_client)
		data["server"] = saveApi(_server)
		data["services"] = services.map { s ->
			mapOf(
				"path" to s.path,
				"lastMethodId" to s.lastMethodId,
				"methods" to s.methods.map { m ->
					mapOf(
						"id" to m.id,
						"name" to m.name,
						"arguments" to m.arguments.map { a ->
							mapOf(
								"name" to a.name,
								"type" to saveType(a.type)
							)
						},
						"result" to m.result?.map { a ->
							mapOf(
								"name" to a.name,
								"type" to saveType(a.type)
							)
						}
					)
				}
			)
		}
	}
	
	private fun loadApi(api: ApiImpl, data: Map<String, Any>) {
		api.path = CodePath(data["path"] as String)
		api.lastServiceId = data["lastServiceId"] as Int
		
		data["services"].asObjectList().mapTo(api.services) {
			ServiceImpl(
				it["id"] as Int,
				it["name"] as String,
				provideServicesDescriptorImpl(it["type"] as String)
			)
		}
	}
	
	private fun saveApi(api: ApiImpl): Map<String, Any> {
		return mapOf(
			"name" to api.path,
			"lastServiceId" to api.lastServiceId,
			"services" to api.services.map {
				mapOf(
					"id" to it.id,
					"name" to it.name,
					"type" to it.descriptor.path
				)
			}
		)
	}
}

