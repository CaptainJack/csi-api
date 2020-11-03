package ru.capjack.csi.api.sandbox.client

import ru.capjack.csi.api.client.ApiSluice
import ru.capjack.csi.api.sandbox.api.SessionUser
import ru.capjack.csi.api.sandbox.api.client.ApiAdapter
import ru.capjack.csi.api.sandbox.api.client.InternalClientApi
import ru.capjack.csi.api.sandbox.api.client.InternalServerApi
import ru.capjack.csi.api.sandbox.api.client.SessionService
import ru.capjack.csi.core.client.Client
import ru.capjack.csi.core.client.ConnectFailReason
import ru.capjack.csi.core.client.ConnectionRecoveryHandler
import ru.capjack.csi.transport.netty.client.WebSocketChannelGate
import ru.capjack.csi.transport.netty.common.factoryEventLoopGroup
import ru.capjack.tool.io.ArrayByteBuffer
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.logging.Logging
import ru.capjack.tool.utils.assistant.ExecutorTemporalAssistant
import ru.capjack.tool.utils.pool.ArrayObjectPool
import ru.capjack.tool.utils.pool.ObjectAllocator
import ru.capjack.tool.utils.pool.ObjectPool
import java.net.URI
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

fun main() {
	val logger = Logging.getLogger("sandbox")
	logger.info("Start")
	
	val processors = Runtime.getRuntime().availableProcessors()
	val assistantExecutor = Executors.newScheduledThreadPool(processors)
	val elg = factoryEventLoopGroup(processors, true)
	
	val assistant = ExecutorTemporalAssistant(assistantExecutor)
	
	val byteBuffers: ObjectPool<ByteBuffer> = ArrayObjectPool(100, object : ObjectAllocator<ByteBuffer> {
		override fun produceInstance(): ByteBuffer = ArrayByteBuffer()
		override fun clearInstance(instance: ByteBuffer): Unit = instance.clear()
		override fun disposeInstance(instance: ByteBuffer): Unit = instance.clear()
	})
	
	val gate = WebSocketChannelGate(elg, URI("ws://localhost:7777"))
	val client = Client(assistant, byteBuffers, gate, version = 1, activityTimeoutSeconds = 10)
	
	val adapter = ApiAdapter(SbApiSluice(), byteBuffers)
	
	Runtime.getRuntime().addShutdownHook(Thread {
		logger.info("Stop elg")
		elg.shutdownGracefully().syncUninterruptibly()
		
		logger.info("Stop assistant")
		assistantExecutor.shutdown()
		if (!assistantExecutor.awaitTermination(1, TimeUnit.MINUTES)) {
			logger.warn("Assistant not stopped")
		}
		
		logger.info("Stopped")
	})
	
	client.connect(byteArrayOf(0, 0, 0, 1), adapter)
}

class SbApiSluice : ApiSluice<InternalClientApi, InternalServerApi> {
	override fun connect(server: InternalServerApi): InternalClientApi {
		println("connect")
		return SbClientApi(server)
	}
	
	override fun fail(reason: ConnectFailReason) {
		println("fail $reason")
	}
	
}

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