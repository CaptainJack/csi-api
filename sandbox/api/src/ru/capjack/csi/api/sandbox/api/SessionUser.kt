package ru.capjack.csi.api.sandbox.api

class SessionUser(
	id: Long,
	name: String,
	val coins: Long
) : User(id, name)