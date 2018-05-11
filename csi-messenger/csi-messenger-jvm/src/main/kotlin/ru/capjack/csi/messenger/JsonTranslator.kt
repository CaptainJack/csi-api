package ru.capjack.csi.messenger

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import ru.capjack.csi.messenger.action.ActionDataType
import java.io.StringReader
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.memberProperties

class JsonTranslator : Translator<String, Any?> {
	private val klaxon = Klaxon()
	
	override fun encodeMessage(message: Message<Any?>): String {
		return klaxon.toJsonString(message)
	}
	
	override fun decodeMessage(message: String): Message<Any?> {
		val json = klaxon.parseJsonObject(StringReader(message))
		val type = json.string("type")
		
		return when (type) {
			Message.Type.ACTION_SEND.name     -> Message.ActionSend(
				json.int("backId")!!,
				json.int("id")!!,
				json["data"],
				json.string("action")!!
			)
			
			Message.Type.ACTION_RESPONSE.name -> Message.ActionResponse(
				json.int("backId")!!,
				json.int("id")!!,
				json["data"],
				json.int("sendId")!!
			)
			
			else                              -> throw IllegalArgumentException(type)
		}
	}
	
	override fun encodeData(data: Any?): Any? {
		return when (data) {
			null,
			is Number,
			is Boolean,
			is String,
			is Collection<*> -> data
			else             -> data::class.memberProperties.associate { it.name to encodeData(it.call(data)) }
		}
	}
	
	override fun <T : Any> decodeData(type: ActionDataType<T>, data: Any?): T {
		return doDecodeData(data, type.kType, type.kClass)
	}
	
	private fun <T : Any> doDecodeData(data: Any?, type: KType, cls: KClass<T>): T {
		@Suppress("UNCHECKED_CAST")
		return when (data) {
			is JsonObject   -> klaxon.fromJsonObject(data, cls.java, cls)
			is JsonArray<*> -> {
				val subType = type.arguments[0].type!!
				val subCls = subType.classifier!! as KClass<*>
				data.map { doDecodeData(it, subType, subCls) }
			}
			else            -> data
		} as T
	}
}

