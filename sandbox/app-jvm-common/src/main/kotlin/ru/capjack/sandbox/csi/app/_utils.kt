package ru.capjack.sandbox.csi.app

import kotlinx.coroutines.runBlocking
import ru.capjack.tool.logging.Logging
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

val LOGGER = Logging.getLogger("sandbox")

val LOGIC_EXECUTOR: ScheduledExecutorService = executor("logic", 3)

fun logic(delay: Long, action: suspend () -> Unit) {
	LOGIC_EXECUTOR.schedule({
		runBlocking { action() }
	}, delay, TimeUnit.MILLISECONDS)
}

fun executor(name: String, size: Int): ScheduledExecutorService {
	return Executors.newScheduledThreadPool(size, ThreadFactory(name))
}


private class ThreadFactory(name: String) : ThreadFactory {
	private val group = System.getSecurityManager()?.threadGroup ?: Thread.currentThread().threadGroup
	private val namePrefix = "$name-"
	private val nextThread = AtomicInteger(1)
	
	override fun newThread(r: Runnable): Thread {
		val t = Thread(group, r, namePrefix + nextThread.getAndIncrement(), 0)
		if (t.isDaemon) t.isDaemon = false
		if (t.priority != Thread.NORM_PRIORITY) t.priority = Thread.NORM_PRIORITY
		return t
	}
}