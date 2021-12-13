package ru.capjack.csi.api

import ru.capjack.csi.core.Connection

abstract class AbstractOuterApi(protected val connection: Connection) : OuterApi {
	override fun closeConnection() {
		connection.closeDueError()
	}
	
	override fun closeConnectionDueError() {
		connection.closeDueError()
	}
}