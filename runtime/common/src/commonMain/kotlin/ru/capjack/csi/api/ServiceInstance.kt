package ru.capjack.csi.api

import ru.capjack.tool.utils.Closeable

interface ServiceInstance<out S : Any> : Closeable {
	val service: S
	
	companion object {
		operator fun <S : Any> invoke(service: S, close: () -> Unit): ServiceInstance<S> {
			return object : ServiceInstance<S> {
				override val service: S = service
				override fun close() = close()
			}
		}
		
		operator fun <S : Closeable> invoke(service: S): ServiceInstance<S> {
			return object : ServiceInstance<S> {
				override val service: S = service
				override fun close() = service.close()
			}
		}
	}
}