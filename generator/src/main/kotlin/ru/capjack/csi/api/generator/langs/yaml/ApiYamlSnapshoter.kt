package ru.capjack.csi.api.generator.langs.yaml

import ru.capjack.csi.api.generator.model.Api
import ru.capjack.csi.api.generator.model.ApiModel
import ru.capjack.csi.api.generator.model.Method
import ru.capjack.tool.biser.generator.langs.yaml.YamlSnapshoter

class ApiYamlSnapshoter : YamlSnapshoter<ApiYamlModel, ApiModel>(ApiYamlModel::class) {
	
	override fun load(model: ApiModel, json: ApiYamlModel) {
		model.version.current = json.version.current
		model.version.compatible = json.version.compatible
		
		loadApi(model, model.client, json.client)
		loadApi(model, model.server, json.server)
		
		json.services.forEach { s ->
			val serviceDescriptor = model.resolveServicesDescriptor(model.resolveEntityName(s.name))
			s.methods.forEach { m ->
				serviceDescriptor.provideMethod(
					m.id,
					m.name,
					m.suspend,
					m.arguments.map { a ->
						if (a.type != null)
							Method.Argument.Value(a.name, model.resolveType(a.type))
						else
							Method.Argument.Subscription(a.name, a.parameters!!.map {
								Method.Parameter(it.name, model.resolveType(it.type))
							})
					},
					m.result?.let {
						when {
							it.startsWith('~') -> Method.Result.InstanceService(model.resolveServicesDescriptor(model.resolveEntityName(it.substring(1))))
							it == "@"          -> Method.Result.Subscription
							else               -> Method.Result.Value(model.resolveType(it))
						}
					}
				)
			}
			serviceDescriptor.commit(s.lastMethodId)
		}
		
		super.load(model, json)
	}
	
	private fun loadApi(model: ApiModel, api: Api, json: ApiYamlModel.Api) {
		json.services.forEach {
			api.provideService(it.id, it.name, model.resolveServicesDescriptor(model.resolveEntityName(it.descriptor)))
		}
		
		api.commit(json.lastServiceId)
	}
	
	override fun save(model: ApiModel, json: ApiYamlModel) {
		saveApi(model.client, json.client)
		saveApi(model.server, json.server)
		
		model.serviceDescriptors.forEach { s ->
			json.services.add(
				ApiYamlModel.Service(
					s.name.toString(),
					s.lastMethodId,
					s.methods.map { m ->
						ApiYamlModel.Service.Method(
							m.id,
							m.name,
							m.suspend,
							m.arguments.map { a ->
								when (a) {
									is Method.Argument.Value        -> ApiYamlModel.Service.Method.Argument(a.name, a.type.toString(), null)
									is Method.Argument.Subscription -> ApiYamlModel.Service.Method.Argument(a.name, null, a.parameters.map { p ->
										ApiYamlModel.Parameter(p.name, p.type.toString())
									}
									)
								}
							},
							m.result?.let {
								when (it) {
									is Method.Result.Value           -> it.type.toString()
									is Method.Result.InstanceService -> "~" + it.descriptor.name.toString()
									Method.Result.Subscription       -> "@"
								}
							}
						)
					}
				)
			)
		}
		
		json.version.current = model.version.current
		json.version.compatible = model.version.compatible
		
		super.save(model, json)
	}
	
	private fun saveApi(api: Api, json: ApiYamlModel.Api) {
		json.lastServiceId = api.lastServiceId
		
		api.services.forEach {
			json.services.add(
				ApiYamlModel.Api.Service(
					it.id,
					it.name,
					it.descriptor.name.toString()
				)
			)
		}
	}
}