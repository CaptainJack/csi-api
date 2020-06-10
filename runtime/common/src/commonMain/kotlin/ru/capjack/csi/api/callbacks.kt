package ru.capjack.csi.api

import ru.capjack.tool.io.biser.BiserReader

typealias Callback<T> = (T) -> Unit

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

expect class RealCallbacksRegister() : CallbacksRegister
