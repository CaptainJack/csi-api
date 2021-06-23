package ru.capjack.csi.api

class InnerServiceHolder {
	private val map = ConcurrentHashMap<Int, InnerServiceHolderItem<*>>()
	private val nextId = AtomicInteger()
	
	fun get(id: Int): InnerServiceDelegate<*>? {
		return map[id]?.delegate
	}
	
	fun close(id: Int): Boolean {
		return null != map.remove(id)?.also {
			it.delegate.close()
			it.instance.close()
		}
	}
	
	fun <S : Any> add(item: InnerServiceHolderItem<S>): Int {
		while (true) {
			val id = nextId.getAndIncrement()
			if (map.putIfAbsent(id, item) == null) {
				item.delegate.setup(id)
				return id
			}
		}
	}
}

