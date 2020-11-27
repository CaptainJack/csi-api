package ru.capjack.csi.api

import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.io.DummyByteBuffer
import ru.capjack.tool.biser.ByteBufferBiserWriter

internal class OutputApiMessageImpl(
	override var buffer: ByteBuffer
) : OutputApiMessage {
	override val writer: ByteBufferBiserWriter = ByteBufferBiserWriter(buffer)
	
	override fun dispose() {
		buffer = DummyByteBuffer
		writer.buffer = DummyByteBuffer
	}
}