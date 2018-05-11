package ru.capjack.csi.messenger.channel

interface Channel<M> {
	fun send(message: M)
	
	fun setReceiver(receiver: (M) -> Unit)
}