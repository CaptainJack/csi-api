package ru.capjack.csi.messenger.channel

class LocalPipe<M> {
	val channel1: Channel<M>
		get() = _channel1
	
	val channel2: Channel<M>
		get() = _channel2
	
	private val _channel1 = PipeChannel1()
	private val _channel2 = PipeChannel2()
	
	
	private inner class PipeChannel1 : AbstractChannel<M>() {
		override fun send(message: M) {
			_channel2.receive(message)
		}
	}
	
	private inner class PipeChannel2 : AbstractChannel<M>() {
		override fun send(message: M) {
			_channel1.receive(message)
		}
	}
}
