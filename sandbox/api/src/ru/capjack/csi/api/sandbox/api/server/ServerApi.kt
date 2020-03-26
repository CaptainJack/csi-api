package ru.capjack.csi.api.sandbox.api.server

interface ServerApi {
	val session: SessionService
	val fiends: FriendsService
}