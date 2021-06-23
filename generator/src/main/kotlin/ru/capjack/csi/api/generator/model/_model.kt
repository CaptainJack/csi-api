package ru.capjack.csi.api.generator.model

import ru.capjack.tool.biser.generator.model.EntityName
import ru.capjack.tool.biser.generator.model.Type
import java.util.*

interface Api {
	val name: String
	val services: Collection<Service>
	val lastServiceId: Int
	
	fun provideService(name: String, descriptor: ServiceDescriptor): Service
	
	fun provideService(id: Int, name: String, descriptor: ServiceDescriptor): Service
	
	fun removeServices(names: Collection<String>)
	
	fun commit(lastServiceId: Int)
}

interface Service {
	val id: Int
	val name: String
	val descriptor: ServiceDescriptor
}

interface ServiceDescriptor {
	val name: EntityName
	val methods: Collection<Method>
	val lastMethodId: Int
	
	fun provideMethod(name: String, suspend: Boolean, arguments: List<Method.Argument>, result: Method.Result?)
	
	fun removeMethods(names: Collection<String>)
	
	fun commit(lastMethodId: Int)
}

interface Method {
	val id: Int
	val name: String
	val suspend: Boolean
	val arguments: List<Argument>
	val result: Result?
	
	class Parameter(val name: String?, val type: Type) {
		override fun equals(other: Any?): Boolean {
			if (this === other) return true
			if (other !is Parameter) return false
			if (name != other.name) return false
			if (type != other.type) return false
			return true
		}
		
		override fun hashCode() = Objects.hash(name, type)
	}
	
	sealed class Argument(val name: String) {
		class Value(name: String, val type: Type) : Argument(name) {
			override fun equals(other: Any?): Boolean {
				if (this === other) return true
				if (other !is Value) return false
				if (name != other.name) return false
				if (type != other.type) return false
				return true
			}
			
			override fun hashCode() = Objects.hash(name, type)
		}
		
		class Subscription(name: String, val parameters: List<Parameter>) : Argument(name) {
			override fun equals(other: Any?): Boolean {
				if (this === other) return true
				if (other !is Subscription) return false
				if (name != other.name) return false
				if (parameters != other.parameters) return false
				return true
			}
			
			override fun hashCode() = Objects.hash(name, parameters)
		}
	}
	
	sealed class Result {
		class Value(val type: Type) : Result() {
			override fun equals(other: Any?): Boolean {
				if (this === other) return true
				if (other !is Value) return false
				if (type != other.type) return false
				return true
			}
			
			override fun hashCode() = type.hashCode()
		}
		
		object Subscription : Result()
		
		class InstanceService(val descriptor: ServiceDescriptor) : Result() {
			override fun equals(other: Any?): Boolean {
				if (this === other) return true
				if (other !is InstanceService) return false
				if (descriptor != other.descriptor) return false
				return true
			}
			
			override fun hashCode() = descriptor.hashCode()
		}
	}
}
