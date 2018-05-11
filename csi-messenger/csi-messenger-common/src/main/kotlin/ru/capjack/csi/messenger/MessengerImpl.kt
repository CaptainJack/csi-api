package ru.capjack.csi.messenger

import ru.capjack.csi.messenger.action.Action
import ru.capjack.csi.messenger.action.ActionDataType
import ru.capjack.csi.messenger.action.Notice
import ru.capjack.csi.messenger.action.ParameterizedNotice
import ru.capjack.csi.messenger.action.ParameterizedRequest
import ru.capjack.csi.messenger.action.Request
import ru.capjack.csi.messenger.channel.Channel

class MessengerImpl<M, D>(
	private val channel: Channel<M>,
	private val translator: Translator<M, D>
) : Messenger {
	private val actionResponseDelegates: MutableMap<Int, ActionResponseDelegate<*>> = mutableMapOf()
	private val actionHandlerDelegates: MutableMap<String, ActionHandlerDelegate> = mutableMapOf()
	private val actionMessageBasket = MessageActionBasket<D>()
	
	private var actionId: Int = 0
	private var actionBackId: Int = 0
	
	init {
		channel.setReceiver(::receiveMessage)
	}
	
	override fun send(notice: Notice) {
		doSend(notice, null, null)
	}
	
	override fun <R : Any> send(request: Request<R>, receiver: (R) -> Unit) {
		doSend(request, null, ActionResponseDelegate(request.responseType, receiver))
	}
	
	override fun <P : Any> send(notice: ParameterizedNotice<P>, parameter: P) {
		doSend(notice, parameter, null)
	}
	
	override fun <P : Any, R : Any> send(request: ParameterizedRequest<P, R>, parameter: P, receiver: (R) -> Unit) {
		doSend(request, parameter, ActionResponseDelegate(request.responseType, receiver))
	}
	
	
	override fun handle(notice: Notice, handler: () -> Unit) {
		doHandle(notice, NoticeHandlerDelegate(handler))
	}
	
	override fun <P : Any> handle(notice: ParameterizedNotice<P>, handler: (P) -> Unit) {
		doHandle(notice, ParameterizedNoticeHandlerDelegate(notice.parameterType, handler))
	}
	
	override fun <R : Any> handle(request: Request<R>, handler: () -> R) {
		handle(request) { response: Response<R> -> response.send(handler.invoke()) }
	}
	
	override fun <P : Any, R : Any> handle(request: ParameterizedRequest<P, R>, handler: (P) -> R) {
		handle(request) { param: P, response: Response<R> -> response.send(handler.invoke(param)) }
	}
	
	override fun <R : Any> handle(request: Request<R>, handler: (Response<R>) -> Unit) {
		doHandle(request, RequestHandlerDelegate(handler))
	}
	
	override fun <P : Any, R : Any> handle(request: ParameterizedRequest<P, R>, handler: (P, Response<R>) -> Unit) {
		doHandle(request, ParameterizedRequestHandlerDelegate(request.parameterType, handler))
	}
	
	override fun repair() {
		sendMessage(Message.ActionRepair(actionBackId))
	}
	
	private fun doSend(action: Action, parameter: Any?, delegate: ActionResponseDelegate<*>?) {
		val message = createMessageActionSend(action.name, parameter)
		if (delegate != null) {
			actionResponseDelegates[message.id] = delegate
		}
		sendMessageAction(message)
	}
	
	private fun doHandle(action: Action, delegate: ActionHandlerDelegate) {
		actionHandlerDelegates[action.name] = delegate
	}
	
	private fun nextActionId() {
		do {
			++actionId
		} while (actionId == 0)
	}
	
	private fun createMessageActionSend(action: String, data: Any?): Message.ActionInteraction<D> {
		nextActionId()
		return Message.ActionSend(
			actionBackId, actionId,
			translator.encodeData(data),
			action
		)
	}
	
	private fun createMessageActionResponse(callId: Int, data: Any?): Message.ActionInteraction<D> {
		nextActionId()
		return Message.ActionResponse(
			actionBackId, actionId,
			translator.encodeData(data),
			callId
		)
	}
	
	private fun sendMessageAction(message: Message.ActionInteraction<D>) {
		actionMessageBasket.add(message)
		sendMessage(message)
	}
	
	private fun sendMessage(message: Message<D>) {
		val encodedMessage = translator.encodeMessage(message)
		channel.send(encodedMessage)
	}
	
	@Suppress("UNCHECKED_CAST")
	private fun receiveMessage(encodedMessage: M) {
		val message = translator.decodeMessage(encodedMessage)
		
		if (message is Message.Action<D>) {
			actionMessageBasket.clearTo(message.backId)
		}
		if (message is Message.ActionInteraction<D>) {
			actionBackId = message.id
		}
		
		when (message) {
			is Message.ActionSend<D>     -> processMessageActionCall(message)
			is Message.ActionResponse<D> -> processMessageActionResponse(message)
		}
	}
	
	private fun processMessageActionCall(message: Message.ActionSend<D>) {
		actionHandlerDelegates[message.action]?.invoke(message.id, message.data)
	}
	
	private fun processMessageActionResponse(message: Message.ActionResponse<D>) {
		actionResponseDelegates.remove(message.sendId)?.invoke(message.data)
	}
	
	
	private inner class ActionResponseDelegate<R : Any>(private val type: ActionDataType<R>, private val receiver: (R) -> Unit) {
		fun invoke(data: D) {
			receiver.invoke(translator.decodeData(type, data))
		}
	}
	
	private abstract inner class ActionHandlerDelegate {
		abstract fun invoke(id: Int, data: D)
	}
	
	private inner class NoticeHandlerDelegate(private val handler: () -> Unit) : ActionHandlerDelegate() {
		override fun invoke(id: Int, data: D) {
			handler.invoke()
		}
	}
	
	private inner class ParameterizedNoticeHandlerDelegate<P : Any>(private val type: ActionDataType<P>, private val handler: (P) -> Unit) : ActionHandlerDelegate() {
		override fun invoke(id: Int, data: D) {
			handler.invoke(translator.decodeData(type, data))
		}
	}
	
	private inner class RequestHandlerDelegate<R>(private val handler: (Response<R>) -> Unit) : ActionHandlerDelegate() {
		override fun invoke(id: Int, data: D) {
			handler.invoke(ResponseImpl(id))
		}
	}
	
	private inner class ParameterizedRequestHandlerDelegate<P : Any, R : Any>(private val type: ActionDataType<P>, private val handler: (P, Response<R>) -> Unit) : ActionHandlerDelegate() {
		override fun invoke(id: Int, data: D) {
			handler.invoke(translator.decodeData(type, data), ResponseImpl(id))
		}
	}
	
	private inner class ResponseImpl<R>(private val id: Int) : Response<R> {
		override fun send(data: R) {
			sendMessageAction(createMessageActionResponse(id, data))
		}
	}
}
