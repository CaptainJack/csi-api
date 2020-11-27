package ru.capjack.csi.api

import ru.capjack.tool.biser.BiserReader

actual class RealCallbacksRegister : CallbacksRegister {
	private var nextId = 0
	private val map = HashMap<Int, BiserReader.(Int) -> Unit>()
	
	override fun put(callback: BiserReader.(Int) -> Unit): Int {
		val id = nextId++
		if (map.contains(id)) {
			throw IllegalStateException("Unprocessed callback $id")
		}
		
		map[id] = callback
		
		return id
	}
	
	override fun take(id: Int): (BiserReader.(Int) -> Unit)? {
		return map.remove(id)
	}
}