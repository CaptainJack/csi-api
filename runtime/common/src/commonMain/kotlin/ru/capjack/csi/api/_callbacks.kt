package ru.capjack.csi.api

import ru.capjack.tool.biser.BiserReader

interface CallbacksRegister {
	fun put(callback: BiserReader.(Int) -> Unit): Int
	
	fun take(id: Int): (BiserReader.(Int) -> Unit)?
}

object NothingCallbacksRegister : CallbacksRegister {
	override fun put(callback: (BiserReader.(Int) -> Unit)): Int {
		throw UnsupportedOperationException()
	}
	
	override fun take(id: Int): (BiserReader.(Int) -> Unit)? {
		throw UnsupportedOperationException()
	}
}

class RealCallbacksRegister() : CallbacksRegister {
	private val nextId = AtomicInteger()
	private val map = ConcurrentHashMap<Int, BiserReader.(Int) -> Unit>()
	
	override fun put(callback: BiserReader.(Int) -> Unit): Int {
		val id = nextId.getAndIncrement()
		if (null == map.putIfAbsent(id, callback)) return id
		
		throw IllegalStateException("Unprocessed callback $id")
	}
	
	override fun take(id: Int): (BiserReader.(Int) -> Unit)? {
		return map.remove(id)
	}
}
