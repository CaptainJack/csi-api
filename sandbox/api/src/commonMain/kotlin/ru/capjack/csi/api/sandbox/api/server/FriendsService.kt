package ru.capjack.csi.api.sandbox.api.server

import ru.capjack.csi.api.Callback
import ru.capjack.csi.api.sandbox.api.data.User
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@ExperimentalJsExport
@JsExport
interface FriendsService {
	fun getFriends(offset: Int, limit: Int, callback: Callback<List<User>>)
}