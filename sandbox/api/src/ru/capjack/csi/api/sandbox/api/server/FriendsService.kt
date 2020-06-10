package ru.capjack.csi.api.sandbox.api.server

import ru.capjack.csi.api.Callback
import ru.capjack.csi.api.sandbox.api.User

interface FriendsService {
	fun getFriends(offset: Int, limit: Int, callback: Callback<List<User>>)
}