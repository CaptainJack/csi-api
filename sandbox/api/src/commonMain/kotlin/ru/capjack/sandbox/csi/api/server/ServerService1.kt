package ru.capjack.sandbox.csi.api.server

import ru.capjack.csi.api.ServiceInstance
import ru.capjack.sandbox.csi.api.data.SealedClass
import ru.capjack.tool.utils.Cancelable

interface ServerService1 {
	fun call()
	
	fun callWithArguments(a: Int, b: String, c: List<SealedClass>, d: Map<Int, SealedClass.SubSealedClass>)
	
	suspend fun callWithResult(): Int
	
	suspend fun callWithArgumentAndResult(a: Int): Int
	
	suspend fun openService(): ServiceInstance<ServerService2>
	
	suspend fun listenOne(handler: (a: Int) -> Unit): Cancelable
}

interface ServerService2 {
	suspend fun sayHello(): String
}