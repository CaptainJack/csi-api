package ru.capjack.csi.api.generator.model

import ru.capjack.tool.biser.generator.CodePath
import ru.capjack.tool.biser.generator.model.Change
import ru.capjack.tool.utils.collections.mutableKeyedSetOf
import java.util.*

internal class ApiImpl(override var path: CodePath) : Api {
	override val services = mutableKeyedSetOf<String, ServiceImpl> { it.name }
	var lastServiceId = 0
	
	override fun updatePath(value: String): Change {
		return if (path.value != value) {
			path = CodePath(value)
			Change.COMPATIBLY
		}
		else Change.ABSENT
	}
	
	override fun provideService(name: String, descriptor: ServiceDescriptor): Change {
		val service = services[name]
		
		if (service == null) {
			services.add(ServiceImpl(++lastServiceId, name, descriptor))
			return Change.COMPATIBLY
		}
		if (service.descriptor != descriptor) {
			service.descriptor = descriptor
			return Change.FULL
		}
		return Change.ABSENT
	}
	
	override fun removeServices(names: Collection<String>): Change {
		val changed = names.fold(false) { r, it -> services.removeKey(it) != null || r }
		return if (changed) Change.FULL else Change.ABSENT
	}
}

internal class ServiceImpl(
	override val id: Int,
	override val name: String,
	override var descriptor: ServiceDescriptor
) : Service


internal class ServiceDescriptorImpl(override val path: CodePath) : ServiceDescriptor {
	override val methods = mutableKeyedSetOf<String, MethodImpl> { it.name }
	var lastMethodId = 0
	
	override fun provideMethod(name: String, arguments: List<Parameter>, result: List<Parameter>?): Change {
		val method = methods.get(name)
		if (method == null) {
			methods.add(MethodImpl(++lastMethodId, name, arguments, result))
			return Change.COMPATIBLY
		}
		return method.update(arguments, result)
	}
	
	override fun removeMethods(names: Collection<String>): Change {
		val changed = names.fold(false) { r, it -> methods.removeKey(it) != null || r }
		return if (changed) Change.FULL else Change.ABSENT
	}
	
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is ServiceDescriptorImpl) return false
		
		if (path != other.path) return false
		
		return true
	}
	
	override fun hashCode(): Int {
		return Objects.hash("ServiceDescriptor", path)
	}
}

internal class MethodImpl(
	override val id: Int,
	override val name: String,
	override var arguments: List<Parameter>,
	override var result: List<Parameter>?
) : Method {
	fun update(arguments: List<Parameter>, result: List<Parameter>?): Change {
		var change = Change.ABSENT
		if (this.arguments != arguments) {
			this.arguments = arguments
			change = Change.FULL
		}
		if (this.result != result) {
			this.result = result
			change = Change.FULL
		}
		return change
	}
}