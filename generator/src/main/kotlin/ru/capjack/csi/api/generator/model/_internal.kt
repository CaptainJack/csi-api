package ru.capjack.csi.api.generator.model

import ru.capjack.tool.biser.generator.model.EntityName
import ru.capjack.tool.biser.generator.model.Model
import ru.capjack.tool.utils.collections.mutableKeyedSetOf
import java.util.*
import kotlin.math.max

internal class ApiImpl(override var name: String) : Api {
	var mutation: Model.Mutation = Model.Mutation.ABSENT
		private set
	
	override val services = mutableKeyedSetOf<String, ServiceImpl> { it.name }
	override var lastServiceId = 0
	
	
	override fun provideService(name: String, descriptor: ServiceDescriptor): Service {
		var service = services[name]
		
		if (service == null) {
			service = ServiceImpl(++lastServiceId, name, descriptor)
			services.add(service)
			raiseMutation(Model.Mutation.COMPATIBLY)
		}
		else if (service.descriptor != descriptor) {
			service.descriptor = descriptor
			raiseMutation(Model.Mutation.FULL)
		}
		
		return service
	}
	
	override fun provideService(id: Int, name: String, descriptor: ServiceDescriptor): Service {
		var service = services[name]
		
		if (service == null) {
			service = ServiceImpl(id, name, descriptor)
			services.add(service)
			raiseMutation(Model.Mutation.COMPATIBLY)
		}
		else {
			if (service.id != id) {
				service.id = id
				raiseMutation(Model.Mutation.FULL)
			}
			if (service.descriptor != descriptor) {
				service.descriptor = descriptor
				raiseMutation(Model.Mutation.FULL)
			}
		}
		
		lastServiceId = max(lastServiceId, id)
		
		return service
	}
	
	override fun removeServices(names: Collection<String>) {
		val changed = names.fold(false) { r, it -> services.removeKey(it) != null || r }
		if (changed) {
			raiseMutation(Model.Mutation.FULL)
		}
	}
	
	override fun commit(lastServiceId: Int) {
		require(lastServiceId >= this.lastServiceId)
		mutation = Model.Mutation.ABSENT
		this.lastServiceId = lastServiceId
	}
	
	private fun raiseMutation(mutation: Model.Mutation) {
		this.mutation = this.mutation.raiseTo(mutation)
	}
}

internal class ServiceImpl(
	override var id: Int,
	override val name: String,
	override var descriptor: ServiceDescriptor
) : Service


internal class ServiceDescriptorImpl(override val name: EntityName) : ServiceDescriptor {
	override val methods = mutableKeyedSetOf<String, MethodImpl> { it.name }
	override var lastMethodId = 0
		private set
	
	var mutation: Model.Mutation = Model.Mutation.ABSENT
		private set
	
	override fun provideMethod(name: String, suspend: Boolean, arguments: List<Method.Argument>, result: Method.Result?) {
		val method = methods[name]
		if (method == null) {
			methods.add(MethodImpl(++lastMethodId, name, suspend, arguments, result))
			raiseMutation(Model.Mutation.COMPATIBLY)
		}
		else {
			if (method.update(suspend, arguments, result)) {
				raiseMutation(Model.Mutation.FULL)
			}
		}
	}
	
	override fun provideMethod(id: Int, name: String, suspend: Boolean, arguments: List<Method.Argument>, result: Method.Result?) {
		val method = methods[name]
		if (method == null) {
			methods.add(MethodImpl(id, name, suspend, arguments, result))
			raiseMutation(Model.Mutation.COMPATIBLY)
		}
		else {
			if (method.id != id) {
				raiseMutation(Model.Mutation.FULL)
			}
			if (method.update(suspend, arguments, result)) {
				raiseMutation(Model.Mutation.FULL)
			}
		}
	}
	
	override fun removeMethods(names: Collection<String>) {
		val changed = names.fold(false) { r, it -> methods.removeKey(it) != null || r }
		if (changed) {
			raiseMutation(Model.Mutation.FULL)
		}
	}
	
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is ServiceDescriptorImpl) return false
		if (name != other.name) return false
		return true
	}
	
	override fun hashCode(): Int {
		return Objects.hash("ServiceDescriptor", name)
	}
	
	override fun commit(lastMethodId: Int) {
		require(lastMethodId >= this.lastMethodId) { "Service $name has lastMethodId=${this.lastMethodId} but commit $lastMethodId" }
		mutation = Model.Mutation.ABSENT
		this.lastMethodId = lastMethodId
	}
	
	private fun raiseMutation(mutation: Model.Mutation) {
		this.mutation = this.mutation.raiseTo(mutation)
	}
}

internal class MethodImpl(
	override val id: Int,
	override val name: String,
	override var suspend: Boolean,
	override var arguments: List<Method.Argument>,
	override var result: Method.Result?
) : Method {
	fun update(suspend: Boolean, arguments: List<Method.Argument>, result: Method.Result?): Boolean {
		this.suspend = suspend
		
		var change = false
		if (this.arguments != arguments) {
			this.arguments = arguments
			change = true
		}
		if (this.result != result) {
			this.result = result
			change = true
		}
		return change
	}
}