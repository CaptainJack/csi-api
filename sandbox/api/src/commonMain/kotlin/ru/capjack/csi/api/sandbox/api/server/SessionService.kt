package ru.capjack.csi.api.sandbox.api.server

import ru.capjack.csi.api.Callback
import ru.capjack.csi.api.sandbox.api.data.SessionUser
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@ExperimentalJsExport
@JsExport
interface SessionService {
	fun getUser(callback: Callback<SessionUser>)
	
	fun addCoins(value: Long, reason: String?)
}