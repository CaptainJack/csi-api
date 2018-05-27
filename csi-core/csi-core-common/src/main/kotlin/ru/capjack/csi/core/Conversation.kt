package ru.capjack.csi.core

import kotlin.reflect.KClass

interface Conversation {
	fun registerReceiver(service: Service)
	
	fun <S : Service> provideSender(service: KClass<S>): S
}