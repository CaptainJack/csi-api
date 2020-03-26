package ru.capjack.csi.api

import ru.capjack.tool.io.biser.BiserReader

interface CallbacksRegister {
	fun put(callback: BiserReader.() -> Unit): Int
	
	fun take(id: Int): (BiserReader.() -> Unit)?
}

object NothingCallbacksRegister : CallbacksRegister {
	override fun put(callback: (BiserReader.() -> Unit)): Int {
		throw UnsupportedOperationException()
	}
	
	override fun take(id: Int): (BiserReader.() -> Unit)? {
		throw UnsupportedOperationException()
	}
}

expect class RealCallbacksRegister() : CallbacksRegister
