package ru.capjack.csi.api.sandbox.api.client

import ru.capjack.csi.api.CallbacksRegister
import ru.capjack.csi.api.ApiMessagePool
import ru.capjack.csi.api.client.AbstractApiConnection
import ru.capjack.csi.core.Connection
import ru.capjack.tool.io.biser.BiserReader
import ru.capjack.tool.logging.Logger
import ru.capjack.csi.api.log
import ru.capjack.csi.api.logS

internal class ApiConnection(
	logger: Logger,
	messagePool: ApiMessagePool,
	connection: Connection,
	callbacks: CallbacksRegister,
	api: InternalClientApi
) : AbstractApiConnection<InternalClientApi>(logger, messagePool, connection, callbacks, api) {
	
	override fun call(serviceId: Int, methodId: Int, message: BiserReader): Boolean {
		return when (serviceId) {
			2 -> call(api.session, methodId, message)
			else -> false
		}
	}
	
	private fun call(service: SessionService, methodId: Int, message: BiserReader): Boolean {
		when (methodId) {
			2 -> {
				val a0 = message.readLong()
				logReceive("session", "updateUserCoins") {
					log("value", a0)
				}
				service.updateUserCoins(a0)
			}
			3 -> {
				val r = message.readInt()
				val a0 = message.readString()
				logReceive("session", "askQuestion", r) {
					log("question", a0)
				}
				service.askQuestion(a0) { r0, r1 ->
					logCallback("session", "askQuestion", r) {
						logS("success", r0)
						log("answer", r1)
					}
					sendResponse(r) {
						writeBoolean(r0)
						writeString(r1)
					}
				}
			}
			4 -> {
				val r = message.readInt()
				val a0 = message.readString()
				logReceive("session", "askQuestionAgain", r) {
					log("question", a0)
				}
				service.askQuestionAgain(a0) { r0, r1 ->
					logCallback("session", "askQuestionAgain", r) {
						logS("success", r0)
						log("answer", r1)
					}
					sendResponse(r) {
						writeBoolean(r0)
						writeString(r1)
					}
				}
			}
			else -> return false
		}
		return true
	}
}
