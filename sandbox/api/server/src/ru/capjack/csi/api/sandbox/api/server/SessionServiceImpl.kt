package ru.capjack.csi.api.sandbox.api.server

import ru.capjack.csi.api.sandbox.api.client.SessionService
import ru.capjack.csi.api.AbstractService
import ru.capjack.csi.api.CallbacksRegister
import ru.capjack.csi.api.OutputApiMessage
import ru.capjack.csi.core.Connection
import ru.capjack.tool.logging.Logger
import ru.capjack.tool.utils.concurrency.ObjectPool
import ru.capjack.csi.api.log
import ru.capjack.csi.api.logS

internal class SessionServiceImpl(
	serviceId: Int,
	serviceName: String,
	logger: Logger,
	writers: ObjectPool<OutputApiMessage>,
	connection: Connection,
	callbacks: CallbacksRegister
) : AbstractService(serviceId, serviceName, logger, writers, connection, callbacks), SessionService {
	
	override fun updateUserCoins(value: Long) {
		logSend("updateUserCoins") {
			log("value", value)
		}
		send(2) {
			writeLong(value)
		}
	}
	
	override fun askQuestion(question: String, callback: (success: Boolean, answer: String) -> Unit) {
		val c = registerCallback{
			val p0 = readBoolean()
			val p1 = readString()
			logCallback("askQuestion", it) {
				logS("success", p0)
				log("answer", p1)
			}
			callback(p0, p1)
		}
		logSend("askQuestion", c) {
			log("question", question)
		}
		send(3, c) {
			writeString(question)
		}
	}
	
	override fun askQuestionAgain(question: String, callback: (success: Boolean, answer: String) -> Unit) {
		val c = registerCallback{
			val p0 = readBoolean()
			val p1 = readString()
			logCallback("askQuestionAgain", it) {
				logS("success", p0)
				log("answer", p1)
			}
			callback(p0, p1)
		}
		logSend("askQuestionAgain", c) {
			log("question", question)
		}
		send(4, c) {
			writeString(question)
		}
	}
}
