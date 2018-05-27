package ru.capjack.csi.core

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.jvm.javaMethod

class SenderFactoryImpl : SenderFactory {
	override fun <S : Service> create(service: KClass<S>): S {
		val descriptor = getDescriptor(service)
		
		@Suppress("UNCHECKED_CAST")
		return Proxy.newProxyInstance(service.java.classLoader, arrayOf(service.java), ServiceInvocationHandler(descriptor)) as S
	}
	
	private val serviceDescriptors: MutableMap<KClass<out Service>, ServiceDescriptor> = ConcurrentHashMap()
	
	private fun getDescriptor(service: KClass<out Service>): ServiceDescriptor {
		@Suppress("UNCHECKED_CAST")
		return serviceDescriptors.computeIfAbsent(service) { ServiceDescriptor(it) }
	}
}

class ServiceDescriptor(type: KClass<out Service>) {
	val name: String = type.simpleName!!
	
	private val methods: Map<Method, MethodDescriptor> = type.declaredMemberFunctions.associate { it.javaMethod!! to MethodDescriptor(it) }
	
	fun getMethod(method: Method?): MethodDescriptor {
		return methods.getValue(method!!)
	}
}

class MethodDescriptor(method: KFunction<*>) {

}

class ServiceInvocationHandler(
	private val descriptor: ServiceDescriptor
) : InvocationHandler {
	
	override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>?) {
		val method = descriptor.getMethod(method)
		
		println("invoke ${descriptor.name}.")
//		println(methods.contains(method))
	}
}