package ru.capjack.csi.api.sandbox.api.server

import ru.capjack.csi.api.sandbox.api.SessionUser

interface SessionService {
	fun getUser(receiver: (user: SessionUser) -> Unit)
	
	fun addCoins(value: Long)
}