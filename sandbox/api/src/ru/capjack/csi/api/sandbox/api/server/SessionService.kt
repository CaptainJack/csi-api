package ru.capjack.csi.api.sandbox.api.server

import ru.capjack.csi.api.Callback
import ru.capjack.csi.api.sandbox.api.SessionUser

interface SessionService {
	fun getUser(callback: Callback<SessionUser>)
	
	fun addCoins(value: Long)
}