package ru.capjack.csi.api

import ru.capjack.tool.utils.Cancelable

class OuterSubscriptionHolder {
	private val map = ConcurrentHashMap<Int, OuterSubscription>()
	private val nextId = AtomicInteger()
	
	fun add(subscription: OuterSubscription, cancelable: Cancelable): Int {
		while (true) {
			val id = nextId.getAndIncrement()
			if (map.putIfAbsent(id, subscription) == null) {
				subscription.setup(id, cancelable)
				return id
			}
		}
	}
	
	fun cancel(id: Int): Boolean {
		return null != map.remove(id)?.also {
			it.cancel()
		}
	}
}