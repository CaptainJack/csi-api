package ru.capjack.csi.api.sandbox.api.server

import ru.capjack.csi.api.ApiMessagePool
import ru.capjack.csi.api.CallbacksRegister
import ru.capjack.csi.api.log
import ru.capjack.csi.api.logS
import ru.capjack.csi.api.sandbox.api.ApiEncoders
import ru.capjack.csi.api.server.AbstractApiConnection
import ru.capjack.csi.core.Connection
import ru.capjack.tool.io.biser.BiserReader
import ru.capjack.tool.logging.Logger

internal class ApiConnection(
	logger: Logger,
	messagePool: ApiMessagePool,
	connection: Connection,
	callbacks: CallbacksRegister,
	api: InternalServerApi
) : AbstractApiConnection<InternalServerApi>(logger, messagePool, connection, callbacks, api) {
	
	override fun call(serviceId: Int, methodId: Int, message: BiserReader): Boolean {
		return when (serviceId) {
			1 -> call(api.session, methodId, message)
			2 -> call(api.fiends, methodId, message)
			else -> false
		}
	}
	
	private fun call(service: SessionService, methodId: Int, message: BiserReader): Boolean {
		when (methodId) {
			1 -> {
				val r = message.readInt()
				logReceive("session", "getUser", r) {
				}
				service.getUser { r0 ->
					logCallback("session", "getUser", r) {
						log(r0, LOG_ENTITY_SessionUser)
					}
					sendResponse(r) {
						write(r0, ApiEncoders.ENTITY_SessionUser)
					}
				}
			}
			2 -> {
				val a0 = message.readLong()
				logReceive("session", "addCoins") {
					log("value", a0)
				}
				service.addCoins(a0)
			}
			else -> return false
		}
		return true
	}
	
	private fun call(service: FriendsService, methodId: Int, message: BiserReader): Boolean {
		when (methodId) {
			1 -> {
				val r = message.readInt()
				val a0 = message.readInt()
				val a1 = message.readInt()
				logReceive("fiends", "getFriends", r) {
					logS("offset", a0)
					log("limit", a1)
				}
				service.getFriends(a0, a1) { r0 ->
					logCallback("fiends", "getFriends", r) {
						log(r0, LOG_ENTITY_User)
					}
					sendResponse(r) {
						writeList(r0, ApiEncoders.ENTITY_User)
					}
				}
			}
			else -> return false
		}
		return true
	}
}
