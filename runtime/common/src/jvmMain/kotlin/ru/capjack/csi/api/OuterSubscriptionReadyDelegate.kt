package ru.capjack.csi.api

import java.util.concurrent.ConcurrentLinkedQueue

actual class OuterSubscriptionReadyDelegate actual constructor(private val subscription: OuterSubscription) {
	private val queue = ConcurrentLinkedQueue<() -> Unit>()
	
	actual fun delay(fn: () -> Unit) {
		synchronized(this) {
			if (!subscription._ready) {
				queue.add(fn)
				return
			}
		}
		fn()
	}
	
	actual fun ready() {
		synchronized(this) {
			check(subscription._ready)
		}
		queue.forEach { it() }
		queue.clear()
	}
}