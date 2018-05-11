package ru.capjack.csi.messenger

import ru.capjack.csi.messenger.action.ActionDataType

interface Translator<M, D> {
	fun encodeMessage(message: Message<D>): M
	
	fun decodeMessage(message: M): Message<D>
	
	fun encodeData(data: Any?): D
	
	fun <T : Any> decodeData(type: ActionDataType<T>, data: D): T
}