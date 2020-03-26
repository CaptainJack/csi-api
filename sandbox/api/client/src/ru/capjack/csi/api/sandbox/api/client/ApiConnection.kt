package ru.capjack.csi.api.sandbox.api.client

import ru.capjack.csi.api.CallbacksRegister
import ru.capjack.csi.api.ApiMessagePool
import ru.capjack.csi.api.client.AbstractApiConnection
import ru.capjack.csi.core.Connection
import ru.capjack.tool.io.biser.BiserReader

internal class ApiConnection(
	messagePool: ApiMessagePool,
	connection: Connection,
	callbacks: CallbacksRegister,
	api: InternalClientApi
) : AbstractApiConnection<InternalClientApi>(messagePool, connection, callbacks, api) {
	
	override fun call(serviceId: Int, methodId: Int, message: BiserReader): Boolean {
		return when (serviceId) {
			1 -> call(api.session, methodId, message)
			else -> false
		}
	}
	
	private fun call(service: SessionService, methodId: Int, message: BiserReader): Boolean {
		when (methodId) {
			1 -> {
				val a0 = message.readLong()
				service.updateUserCoins(a0)
			}
			2 -> {
				val a0 = message.readLong()
				service.updateUserLevel(a0)
			}
			3 -> {
				val r = message.readInt()
				val a0 = message.readString()
				service.askQuestion(a0) { r0, r1 ->
					sendResponse(r) {
						writeBoolean(r0)
						writeString(r1)
					}
				}
			}
			4 -> {
				val r = message.readInt()
				val a0 = message.readString()
				service.askQuestionAgain(a0) { r0, r1 ->
					sendResponse(r) {
						writeBoolean(r0)
						writeString(r1)
					}
				}
			}
		}
		return true
	}
}
