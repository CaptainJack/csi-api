package ru.capjack.sandbox.csi.app.server

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import ru.capjack.csi.api.AtomicInteger
import ru.capjack.csi.api.ServiceInstance
import ru.capjack.sandbox.csi.app.LOGGER
import ru.capjack.sandbox.csi.app.LOGIC_EXECUTOR
import ru.capjack.sandbox.csi.app.executor
import ru.capjack.sandbox.csi.app.logic
import ru.capjack.csi.api.server.ApiSluice
import ru.capjack.csi.core.server.ConnectionAuthorizer
import ru.capjack.csi.core.server.Server
import ru.capjack.csi.transport.netty.server.ServerEventLoopGroupsImpl
import ru.capjack.csi.transport.netty.server.WebSocketChannelGate
import ru.capjack.sandbox.csi.api.data.SealedClass
import ru.capjack.sandbox.csi.api.server.ApiAdapter
import ru.capjack.sandbox.csi.api.server.InternalClientApi
import ru.capjack.sandbox.csi.api.server.InternalServerApi
import ru.capjack.sandbox.csi.api.server.ServerService1
import ru.capjack.sandbox.csi.api.server.ServerService2
import ru.capjack.tool.io.ArrayByteBuffer
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.io.getInt
import ru.capjack.tool.logging.info
import ru.capjack.tool.utils.Cancelable
import ru.capjack.tool.utils.assistant.ExecutorTemporalAssistant
import ru.capjack.tool.utils.pool.ArrayObjectPool
import ru.capjack.tool.utils.pool.ObjectAllocator
import ru.capjack.tool.utils.pool.ObjectPool
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread


fun main() {
	LOGGER.info("Start")
	
	val assistantExecutor = executor("assistant", Runtime.getRuntime().availableProcessors())
	val elg = ServerEventLoopGroupsImpl()
	
	val byteBuffers: ObjectPool<ByteBuffer> = ArrayObjectPool(100, object : ObjectAllocator<ByteBuffer> {
		override fun produceInstance(): ByteBuffer = ArrayByteBuffer()
		override fun clearInstance(instance: ByteBuffer): Unit = instance.clear()
		override fun disposeInstance(instance: ByteBuffer): Unit = instance.clear()
	})
	
	val assistant = ExecutorTemporalAssistant(assistantExecutor)
	val connectionAuthorizer = SbConnectionAuthorizer()
	val connectionAcceptor = ApiAdapter(SbApiSluice(), CoroutineScope(assistantExecutor.asCoroutineDispatcher()), byteBuffers)
	val gate = WebSocketChannelGate(elg, "localhost:7777")
	
	val server = Server(
		assistant,
		byteBuffers,
		connectionAuthorizer,
		connectionAcceptor,
		gate,
		shutdownTimeoutSeconds = 10,
		version = 1,
		channelActivityTimeoutSeconds = 10
	)

//	logic(5000) { exitProcess(0) }
	
	Runtime.getRuntime().addShutdownHook(thread(name = "shutdown", start = false) {
		LOGGER.info("Stop server")
		server.stop()
		
		LOGGER.info("Stop elg")
		elg.stop()
		
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
}


class SbConnectionAuthorizer() : ConnectionAuthorizer<Int> {
	override fun authorizeConnection(key: ByteArray): Int? {
		return if (key.size >= 4) key.getInt(0).takeIf { it > 0 }
		else null
	}
}

class SbApiSluice : ApiSluice<Int, InternalServerApi, InternalClientApi> {
	override fun connect(identity: Int, client: InternalClientApi): InternalServerApi {
		return SbServerApi(identity, client)
	}
}

class SbServerApi(private val identity: Int, private val client: InternalClientApi) : InternalServerApi {
	override val service1 = object : ServerService1 {
		override fun call() {
			LOGGER.info { "Called service1.empty" }
		}
		
		override fun callWithArguments(a: Int, b: String, c: List<SealedClass>, d: Map<Int, SealedClass.SubSealedClass>) {
			LOGGER.info { "Called service1.withParameters" }
		}
		
		override suspend fun callWithResult(): Int {
			return 5
		}
		
		override suspend fun callWithArgumentAndResult(a: Int): Int {
			delay(1000)
			return a
		}
		
		private val counter = AtomicInteger()
		
		override suspend fun openService(): ServiceInstance<ServerService2> {
			val i = counter.incrementAndGet()
			return ServiceInstance(object : ServerService2 {
				override suspend fun sayHello() = "Hello $i!"
			}) {
				LOGGER.info("Closed sub service $i")
			}
		}
		
		override suspend fun listenOne(handler: (a: Int) -> Unit): Cancelable {
			logic(1000) { handler(1) }
			logic(2000) { handler(2) }
			
			return Cancelable {
				LOGGER.info("listenOne canceled")
			}
		}
	}
	
	override fun handleConnectionClose() {
		println("$identity handleConnectionClose")
	}
}