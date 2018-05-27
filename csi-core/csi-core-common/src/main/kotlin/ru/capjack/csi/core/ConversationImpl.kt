package ru.capjack.csi.core

import kotlin.reflect.KClass

class ConversationImpl(
	private val senderFactory: SenderFactory
) : Conversation {
	
	override fun registerReceiver(service: Service) {
	
	}
	
	override fun <S : Service> provideSender(service: KClass<S>): S {
		return senderFactory.create(service)
	}
}

interface SenderFactory {
	fun <S : Service> create(service: KClass<S>): S
}
