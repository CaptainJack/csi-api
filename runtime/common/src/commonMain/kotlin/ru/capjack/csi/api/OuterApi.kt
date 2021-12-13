package ru.capjack.csi.api

interface OuterApi {
	fun closeConnection()
	
	fun closeConnectionDueError()
}