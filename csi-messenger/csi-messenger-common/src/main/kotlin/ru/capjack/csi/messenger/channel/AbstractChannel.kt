package ru.capjack.csi.messenger.channel

abstract class AbstractChannel<M> : Channel<M> {
	private var receiver: ((M) -> Unit)? = null
	
	override fun setReceiver(receiver: (M) -> Unit) {
		this.receiver = receiver
	}
	
	fun receive(message: M) {
		receiver?.invoke(message)
	}
}