package ru.capjack.csi.api

import ru.capjack.tool.biser.ByteBufferBiserReader
import ru.capjack.tool.utils.pool.ObjectPool

interface ApiMessagePool {
	val readers: ObjectPool<ByteBufferBiserReader>
	val writers: ObjectPool<OutputApiMessage>
}

