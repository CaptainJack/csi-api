package ru.capjack.csi.api.sandbox.api.server

import ru.capjack.csi.api.sandbox.api.client.SessionService
import ru.capjack.csi.api.AbstractService
import ru.capjack.csi.api.CallbacksRegister
import ru.capjack.csi.api.OutputApiMessage
import ru.capjack.csi.core.Connection
import ru.capjack.tool.utils.concurrency.ObjectPool

internal class SessionServiceImpl(
	serviceId: Int,
	writers: ObjectPool<OutputApiMessage>,
	connection: Connection,
	callbacks: CallbacksRegister
) : AbstractService(serviceId, writers, connection, callbacks), SessionService {
	
	override fun updateUserCoins(value: Long) {
		send(1) {
			writeLong(value)
		}
	}
	
	override fun updateUserLevel(value: Long) {
		send(2) {
			writeLong(value)
		}
	}
	
	override fun askQuestion(question: String, receiver: (success: Boolean, answer: String) -> Unit) {
		send(3, {
			val p0 = readBoolean()
			val p1 = readString()
			receiver(p0, p1)
		}) {
			writeString(question)
		}
	}
	
	override fun askQuestionAgain(question: String, receiver: (success: Boolean, answer: String) -> Unit) {
		send(4, {
			val p0 = readBoolean()
			val p1 = readString()
			receiver(p0, p1)
		}) {
			writeString(question)
		}
	}
}
