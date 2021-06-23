package ru.capjack.csi.api

internal class OuterServiceInstance<S : OuterService>(
	override val service: S
) : ServiceInstance<S> {
	
	override fun close() {
		service._close()
	}
}
