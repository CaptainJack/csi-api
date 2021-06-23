package ru.capjack.csi.api.generator.model

import ru.capjack.csi.api.generator.ApiVersion
import ru.capjack.tool.biser.generator.model.DefaultModel
import ru.capjack.tool.biser.generator.model.EntityName
import ru.capjack.tool.biser.generator.model.Model
import ru.capjack.tool.utils.collections.getOrAdd
import ru.capjack.tool.utils.collections.mutableKeyedSetOf

class ApiModel : DefaultModel() {
	private val _client = ApiImpl("ClientApi")
	private val _server = ApiImpl("ServerApi")
	
	val version = ApiVersion()
	
	val client: Api get() = _client
	val server: Api get() = _server
	
	private val _serviceDescriptors = mutableKeyedSetOf(ServiceDescriptorImpl::name)
	private val deprecatedServicesNames = mutableSetOf<EntityName>()
	
	override val mutation: Model.Mutation
		get() = _serviceDescriptors
			.fold(super.mutation) { m, d -> m.raiseTo(d.mutation) }
			.raiseTo(_client.mutation)
			.raiseTo(_server.mutation)
	
	val serviceDescriptors: Collection<ServiceDescriptor>
		get() = _serviceDescriptors
	
	fun resolveServicesDescriptor(name: EntityName): ServiceDescriptor {
		deprecatedServicesNames.remove(name)
		return _serviceDescriptors.getOrAdd(name, ::ServiceDescriptorImpl)
	}
	
	override fun commit(lastEntityId: Int) {
		super.commit(lastEntityId)
		deprecatedServicesNames.addAll(serviceDescriptors.map(ServiceDescriptor::name))
	}
	
	fun removeDeprecatedServices() {
		val changed = deprecatedServicesNames.fold(false) { r, it -> _serviceDescriptors.removeKey(it) != null || r }
		if (changed) {
			raiseMutation(Model.Mutation.FULL)
		}
		deprecatedServicesNames.clear()
	}
}

