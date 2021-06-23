package ru.capjack.csi.api


actual class ConcurrentHashMap<K, V> : HashMap<K, V>(), MutableMap<K, V> {
	actual fun putIfAbsent(k: K, v: V): V? {
		val o = get(k)
		if (o == null) {
			put(k, v)
		}
		return o
	}
}

actual class AtomicInteger {
	private var value = 0
	
	actual fun getAndIncrement(): Int {
		return value++
	}
}