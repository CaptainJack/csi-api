package ru.capjack.csi.messenger

import ru.capjack.csi.messenger.action.ActionDataType
import ru.capjack.ktjs.common.js.jso

class JsonTranslator : Translator<String, dynamic> {
	private val dataDecoders: MutableMap<ActionDataType<*>, (json: dynamic) -> Any> = mutableMapOf()
	
	fun <T : Any> setDataDecoder(type: ActionDataType<T>, function: (data: dynamic) -> T) {
		dataDecoders[type] = function
	}
	
	override fun encodeMessage(message: Message<dynamic>): String {
		val o: dynamic = jso {
			type = message.type.name
		}
		
		if (message is Message.Action) {
			o.backId = message.backId
		}
		if (message is Message.ActionInteraction) {
			o.id = message.id
			o.data = message.data
		}
		if (message is Message.ActionSend) {
			o.action = message.action
		}
		else if (message is Message.ActionResponse) {
			o.sendId = message.sendId
		}
		
		return JSON.stringify(o)
	}
	
	override fun decodeMessage(message: String): Message<dynamic> {
		val json: dynamic = JSON.parse(message)
		val type = Message.Type.valueOf(json["type"] as String)
		
		return when (type) {
			Message.Type.ACTION_SEND     -> Message.ActionSend(
				json.backId as Int,
				json.id as Int,
				json.data,
				json.action as String
			)
			
			Message.Type.ACTION_RESPONSE -> Message.ActionResponse(
				json.backId as Int,
				json.id as Int,
				json.data,
				json.sendId as Int
			)
			
			else                         -> throw IllegalArgumentException(message)
		}
	}
	
	override fun encodeData(data: Any?): Any? {
		return data
	}
	
	override fun <T : Any> decodeData(type: ActionDataType<T>, data: dynamic): T {
		@Suppress("UNCHECKED_CAST")
		return dataDecoders[type]?.invoke(data) as T
	}
}