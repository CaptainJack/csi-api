package ru.capjack.csi.api

expect class ConcurrentHashMap<K, V>() : MutableMap<K, V> {
	fun putIfAbsent(k: K, v: V): V?
}

expect class AtomicInteger() {
	fun getAndIncrement(): Int
}