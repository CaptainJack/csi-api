package ru.capjack.csi.api.sandbox.client

import kotlinx.browser.window
import ru.capjack.csi.api.client.ApiSluice
import ru.capjack.csi.api.sandbox.api.client.ApiAdapter
import ru.capjack.csi.api.sandbox.api.client.InternalClientApi
import ru.capjack.csi.api.sandbox.api.client.InternalServerApi
import ru.capjack.csi.api.sandbox.api.client.SessionService
import ru.capjack.csi.api.sandbox.api.data.SessionUser
import ru.capjack.csi.api.sandbox.api.data.User
import ru.capjack.csi.core.client.Client
import ru.capjack.csi.core.client.ConnectFailReason
import ru.capjack.csi.core.client.ConnectionRecoveryHandler
import ru.capjack.csi.transport.js.client.browser.WebSocketChannelGate
import ru.capjack.tool.io.ArrayByteBuffer
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.logging.Logging
import ru.capjack.tool.utils.ErrorCatcher
import ru.capjack.tool.utils.InstantTime
import ru.capjack.tool.utils.assistant.WgsTemporalAssistant
import ru.capjack.tool.utils.pool.ArrayObjectPool
import ru.capjack.tool.utils.pool.ObjectAllocator
import ru.capjack.tool.utils.pool.ObjectPool

@ExperimentalJsExport
@JsExport
fun main(sluice: ApiSluice<InternalClientApi, InternalServerApi>) {
	val logger = Logging.getLogger("sandbox")
	
	val byteBuffers: ObjectPool<ByteBuffer> = ArrayObjectPool(100, object : ObjectAllocator<ByteBuffer> {
		override fun produceInstance(): ByteBuffer = ArrayByteBuffer()
		override fun clearInstance(instance: ByteBuffer): Unit = instance.clear()
		override fun disposeInstance(instance: ByteBuffer): Unit = instance.clear()
	})
	
	val assistant = WgsTemporalAssistant(
		object : ErrorCatcher {
			override fun catchError(error: dynamic) = logger.error("WGS Error: $error")
		},
		window.performance.unsafeCast<InstantTime>(),
		window
	)
	
	val gate = WebSocketChannelGate("ws://localhost:7777")
	val client = Client(assistant, byteBuffers, gate, 1, 10)
	val adapter = ApiAdapter(sluice, byteBuffers)
	
	client.connect(byteArrayOf(0, 0, 0, 1), adapter)
}

@ExperimentalJsExport
class SbApiSluice : ApiSluice<InternalClientApi, InternalServerApi> {
	override fun connect(server: InternalServerApi): InternalClientApi {
		println("connect")
		return SbClientApi(server)
	}
	
	override fun fail(reason: ConnectFailReason) {
		println("fail $reason")
	}
	
}

@ExperimentalJsExport
class SbClientApi(server: InternalServerApi) : InternalClientApi, ConnectionRecoveryHandler {
	
	private lateinit var sessionUser: SessionUser
	
	override val session: SessionService = object : SessionService {
		override fun updateUserCoins(value: Long) {
			println("updateUserCoins $value")
			sessionUser.coins = value
		}
		
		override fun askQuestion(question: String, callback: (success: Boolean, answer: String) -> Unit) {
			callback(true, "$question Ха-ха-ха")
		}
		
		override fun askQuestionAgain(question: String, callback: (success: Boolean, answer: String) -> Unit) {
			callback(false, "$question Хи-хи-хи")
		}
		
	}
	
	init {
		server.session.getUser {
			println("sessionUser")
			sessionUser = it
		}
	}
	
	override fun handleConnectionCloseTimeout(seconds: Int) {
		println("handleConnectionCloseTimeout $seconds")
	}
	
	override fun handleConnectionLost(): ConnectionRecoveryHandler {
		println("handleConnectionLost")
		return this
	}
	
	override fun handleConnectionRecovered() {
		println("handleConnectionRecovered")
	}
	
	override fun handleConnectionClose() {
		println("handleConnectionClose")
	}
	
}