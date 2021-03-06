package ru.capjack.csi.api.generator.model

import ru.capjack.tool.biser.generator.CodePath
import ru.capjack.tool.biser.generator.model.Change
import ru.capjack.tool.biser.generator.model.Type

interface Api {
	fun updatePath(value: String): Change
	fun provideService(name: String, descriptor: ServiceDescriptor): Change
	fun removeServices(names: Collection<String>): Change
	
	val path: CodePath
	val services: Collection<Service>
}

interface Service {
	val id: Int
	val name: String
	val descriptor: ServiceDescriptor
}

interface ServiceDescriptor {
	val path: CodePath
	val methods: Collection<Method>
	
	fun provideMethod(name: String, arguments: List<Parameter>, result: List<Parameter>?): Change
	fun removeMethods(names: Collection<String>): Change
}

interface Method {
	val id: Int
	val name: String
	val arguments: List<Parameter>
	val result: List<Parameter>?
}

class Parameter(val name: String?, val type: Type) {
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is Parameter) return false
		
		if (name != other.name) return false
		if (type != other.type) return false
		
		return true
	}
	
	override fun hashCode(): Int {
		var result = name?.hashCode() ?: 0
		result = 31 * result + type.hashCode()
		return result
	}
}