package ru.capjack.csi.api

import ru.capjack.tool.utils.collections.ArrayQueue

actual class OuterSubscriptionReadyDelegate actual constructor(private val subscription: OuterSubscription) {
	private val queue = ArrayQueue<() -> Unit>()
	
	actual fun delay(fn: () -> Unit) {
		if (!subscription._ready) {
			queue.add(fn)
			return
		}
		fn()
	}
	
	actual fun ready() {
		check(subscription._ready)
		queue.forEach { it() }
		queue.clear()
	}
}