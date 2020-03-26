package ru.capjack.csi.api

import ru.capjack.tool.io.biser.BiserReader
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

actual class RealCallbacksRegister : CallbacksRegister {
	private val nextId = AtomicInteger()
	private val map = ConcurrentHashMap<Int, BiserReader.() -> Unit>()
	
	override fun put(callback: BiserReader.() -> Unit): Int {
		val id = nextId.getAndIncrement()
		if (null == map.putIfAbsent(id, callback)) return id
		
		throw IllegalStateException("Unprocessed callback $id")
	}
	
	override fun take(id: Int): (BiserReader.() -> Unit)? {
		return map.remove(id)
	}
}