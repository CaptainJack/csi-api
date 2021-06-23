package ru.capjack.csi.api

class InnerSubscriptionHolder {
	private val map = ConcurrentHashMap<Int, InnerSubscription>()
	
	fun add(subscription: InnerSubscription) {
		map[subscription.id] = subscription
	}
	
	fun get(id: Int): InnerSubscription? {
		return map[id]
	}
	
	fun remove(id: Int) {
		map.remove(id)
	}
	
	fun cancelAll() {
		map.values.toList().forEach(InnerSubscription::cancel)
	}
}

