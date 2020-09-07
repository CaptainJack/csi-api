package ru.capjack.csi.api.sandbox.api

class SessionUser(
	id: Long,
	name: String?,
	var coins: Long
) : User(id, name)