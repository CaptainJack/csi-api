package ru.capjack.csi.api.sandbox.server

import ru.capjack.csi.api.sandbox.api.SessionUser
import ru.capjack.csi.api.sandbox.api.User
import ru.capjack.csi.api.sandbox.api.server.ApiAdapter
import ru.capjack.csi.api.sandbox.api.server.FriendsService
import ru.capjack.csi.api.sandbox.api.server.InternalClientApi
import ru.capjack.csi.api.sandbox.api.server.InternalServerApi
import ru.capjack.csi.api.sandbox.api.server.SessionService
import ru.capjack.csi.api.server.ApiSluice
import ru.capjack.csi.core.server.ConnectionAuthorizer
import ru.capjack.csi.core.server.Server
import ru.capjack.csi.transport.netty.server.ServerEventLoopGroupsImpl
import ru.capjack.csi.transport.netty.server.WebSocketChannelGate
import ru.capjack.tool.io.ArrayByteBuffer
import ru.capjack.tool.io.ByteBuffer
import ru.capjack.tool.io.InputByteBuffer
import ru.capjack.tool.logging.Logging
import ru.capjack.tool.utils.concurrency.ArrayObjectPool
import ru.capjack.tool.utils.concurrency.ExecutorDelayableAssistant
import ru.capjack.tool.utils.concurrency.ObjectAllocator
import ru.capjack.tool.utils.concurrency.ObjectPool
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


fun main() {
	val logger = Logging.getLogger("sandbox")
	logger.info("Start")
	
	val assistantExecutor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors())
	val elg = ServerEventLoopGroupsImpl()
	
	val byteBuffers: ObjectPool<ByteBuffer> = ArrayObjectPool(100, object : ObjectAllocator<ByteBuffer> {
		override fun produceInstance(): ByteBuffer = ArrayByteBuffer()
		override fun clearInstance(instance: ByteBuffer): Unit = instance.clear()
		override fun disposeInstance(instance: ByteBuffer): Unit = instance.clear()
	})
	
	val assistant = ExecutorDelayableAssistant(assistantExecutor)
	val connectionAuthorizer = SbConnectionAuthorizer()
	val connectionAcceptor = ApiAdapter(SbApiSluice(), byteBuffers)
	val gate = WebSocketChannelGate(elg, "localhost:7777")
	
	val server = Server(
		assistant,
		byteBuffers,
		connectionAuthorizer,
		connectionAcceptor,
		gate,
		shutdownTimeoutSeconds = 1,
		version = 1,
		channelActivityTimeoutSeconds = 10
	)
	
	Runtime.getRuntime().addShutdownHook(Thread {
		logger.info("Stop server")
		server.stop()
		
		logger.info("Stop elg")
		elg.stop()
		
		logger.info("Stop assistant")
		assistantExecutor.shutdown()
		if (!assistantExecutor.awaitTermination(1, TimeUnit.MINUTES)) {
			logger.warn("Assistant not stopped")
		}
		
		logger.info("Stopped")
	})
}


class SbConnectionAuthorizer() : ConnectionAuthorizer<Int> {
	override fun authorizeConnection(authorizationKey: InputByteBuffer): Int? {
		return if (authorizationKey.isReadable(4)) authorizationKey.readInt().takeIf { it > 0 }
		else null
	}
}

class SbApiSluice : ApiSluice<Int, InternalServerApi, InternalClientApi> {
	override fun connect(identity: Int, client: InternalClientApi): InternalServerApi {
		return SbServerApi(identity, client)
	}
}

class SbServerApi(private val identity: Int, private val client: InternalClientApi) : InternalServerApi {
	val sessionUser = SessionUser(1, "Леша", 1000)
	
	override val session: SessionService = object : SessionService {
		override fun getUser(callback: (user: SessionUser) -> Unit) {
			println("$identity getUser")
			callback(sessionUser)
		}
		
		override fun addCoins(value: Long) {
			println("$identity addCoins $value")
			sessionUser.coins += value
			client.session.updateUserCoins(sessionUser.coins)
		}
	}
	
	override val fiends: FriendsService = object : FriendsService {
		override fun getFriends(offset: Int, limit: Int, callback: (List<User>) -> Unit) {
			println("$identity getFriends $offset $limit")
			callback(listOf(User(2, "User 2"), User(3, "User 3", User.Rank.MAJOR)))
		}
		
	}
	
	override fun handleConnectionClose() {
		println("$identity handleConnectionClose")
	}
}