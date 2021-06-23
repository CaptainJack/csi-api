package ru.capjack.sandbox.csi.app.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import ru.capjack.csi.api.client.ApiSluice
import ru.capjack.csi.core.client.Client
import ru.capjack.csi.core.client.ConnectFailReason
import ru.capjack.csi.core.client.ConnectionRecoveryHandler
import ru.capjack.csi.transport.netty.client.WebSocketChannelGate
import ru.capjack.csi.transport.netty.common.factoryEventLoopGroup
import ru.capjack.sandbox.csi.api.client.ApiAdapter
import ru.capjack.sandbox.csi.api.client.InternalClientApi
import ru.capjack.sandbox.csi.api.client.InternalServerApi
import ru.capjack.sandbox.csi.api.data.SealedClass
import ru.capjack.sandbox.csi.app.LOGGER
import ru.capjack.sandbox.csi.app.LOGIC_EXECUTOR
import ru.capjack.sandbox.csi.app.executor
import ru.capjack.sandbox.csi.app.logic
import ru.capjack.tool.io.ArrayByteBuffer
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.utils.assistant.ExecutorTemporalAssistant
import ru.capjack.tool.utils.pool.ArrayObjectPool
import ru.capjack.tool.utils.pool.ObjectAllocator
import ru.capjack.tool.utils.pool.ObjectPool
import java.net.URI
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

fun main() {
	LOGGER.info("Start")
	
	val processors = Runtime.getRuntime().availableProcessors()
	val assistantExecutor = executor("assistant", processors)
	val elg = factoryEventLoopGroup(processors, true)
	
	val assistant = ExecutorTemporalAssistant(assistantExecutor)
	
	val byteBuffers: ObjectPool<ByteBuffer> = ArrayObjectPool(100, object : ObjectAllocator<ByteBuffer> {
		override fun produceInstance(): ByteBuffer = ArrayByteBuffer()
		override fun clearInstance(instance: ByteBuffer): Unit = instance.clear()
		override fun disposeInstance(instance: ByteBuffer): Unit = instance.clear()
	})
	
	val gate = WebSocketChannelGate(elg, URI("ws://localhost:7777"))
	val client = Client(assistant, byteBuffers, gate, version = 1)
	
	val adapter = ApiAdapter(SbApiSluice(), CoroutineScope(assistantExecutor.asCoroutineDispatcher()), byteBuffers)
	
	Runtime.getRuntime().addShutdownHook(thread(name = "shutdown", start = false) {
		LOGGER.info("Stop elg")
		elg.shutdownGracefully().syncUninterruptibly()
		
		LOGGER.info("Stop assistant")
		assistantExecutor.shutdown()
		if (!assistantExecutor.awaitTermination(1, TimeUnit.MINUTES)) {
			LOGGER.warn("Assistant not stopped")
		}
		
		LOGGER.info("Stop logic")
		LOGIC_EXECUTOR.shutdown()
		if (!assistantExecutor.awaitTermination(1, TimeUnit.MINUTES)) {
			LOGGER.warn("Logic not stopped")
		}
		
		LOGGER.info("Stopped")
	})
	
	client.connect(byteArrayOf(0, 0, 0, 1), adapter)
}

class SbApiSluice : ApiSluice<InternalClientApi, InternalServerApi> {
	override fun connect(server: InternalServerApi): InternalClientApi {
		LOGGER.info("Connect")
		return SbClientApi(server)
	}
	
	override fun fail(reason: ConnectFailReason) {
		LOGGER.info("Fail $reason")
	}
	
}

class SbClientApi(server: InternalServerApi) : InternalClientApi, ConnectionRecoveryHandler {
	
	init {
		logic(100) {
			server.service1.call()
		}
		
		logic(200) {
			server.service1.callWithArguments(
				1, "2",
				listOf(SealedClass.SubClass(3), SealedClass.SubObject, SealedClass.SubSealedClass.SubSubClass(3), SealedClass.SubSealedClass.SubSubObject),
				mapOf(
					4 to SealedClass.SubSealedClass.SubSubClass(4),
					5 to SealedClass.SubSealedClass.SubSubObject
				)
			)
		}
		
		logic(300) {
			server.service1.callWithResult()
		}
		
		logic(400) {
			server.service1.callWithArgumentAndResult(42)
		}
		
		logic(600) {
			val cancelable = server.service1.listenOne {
				LOGGER.info("listenOne $it")
			}
			logic(3000) {
				cancelable.cancel()
			}
		}
		
		logic(4000) {
			val instance = server.service1.openService()
			logic(100) {
				instance.service.sayHello()
			}
			logic(2000) {
				instance.close()
			}
		}
		
		logic(5000) {
			val instance = server.service1.openService()
			logic(100) {
				instance.service.sayHello()
			}
			logic(2000) {
				instance.close()
			}
		}
	}
	
	override fun handleConnectionCloseTimeout(seconds: Int) {
		LOGGER.info("handleConnectionCloseTimeout $seconds")
	}
	
	override fun handleConnectionLost(): ConnectionRecoveryHandler {
		LOGGER.info("handleConnectionLost")
		return this
	}
	
	override fun handleConnectionRecovered() {
		LOGGER.info("handleConnectionRecovered")
	}
	
	override fun handleConnectionClose() {
		LOGGER.info("handleConnectionClose")
	}
	
}