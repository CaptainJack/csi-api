package ru.capjack.csi.api

import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.biser.BiserWriter

interface OutputApiMessage {
	val buffer: ByteBuffer
	val writer: BiserWriter
	
	fun dispose()
}