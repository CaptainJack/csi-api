package ru.capjack.csi.api.sandbox.api.server

import ru.capjack.csi.api.CallbacksRegister
import ru.capjack.csi.api.ApiMessagePool
import ru.capjack.csi.api.server.AbstractApiConnection
import ru.capjack.csi.core.Connection
import ru.capjack.tool.io.biser.BiserReader
import ru.capjack.csi.api.sandbox.api.ApiEncoders

internal class ApiConnection(
	messagePool: ApiMessagePool,
	connection: Connection,
	callbacks: CallbacksRegister,
	api: InternalServerApi
) : AbstractApiConnection<InternalServerApi>(messagePool, connection, callbacks, api) {
	
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
				service.getUser { r0 ->
					sendResponse(r) {
						write(r0, ApiEncoders.ENTITY_SessionUser)
					}
				}
			}
			2 -> {
				val a0 = message.readLong()
				service.addCoins(a0)
			}
		}
		return true
	}
	
	private fun call(service: FriendsService, methodId: Int, message: BiserReader): Boolean {
		when (methodId) {
			1 -> {
				val r = message.readInt()
				val a0 = message.readInt()
				val a1 = message.readInt()
				service.getFriends(a0, a1) { r0 ->
					sendResponse(r) {
						writeList(r0, ApiEncoders.ENTITY_User)
					}
				}
			}
		}
		return true
	}
}
