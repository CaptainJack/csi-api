package ru.capjack.csi.messenger

sealed class Message<D>(
	val type: Type
) {
	enum class Type {
		ACTION_SEND,
		ACTION_RESPONSE,
		ACTION_REPAIR
	}
	
	abstract class Action<D>(
		type: Type,
		val backId: Int
	) : Message<D>(type)
	
	abstract class ActionInteraction<D>(
		type: Type,
		backId: Int,
		val id: Int,
		val data: D
	) : Action<D>(type, backId)
	
	class ActionSend<D>(
		backId: Int,
		id: Int,
		data: D,
		val action: String
	) : ActionInteraction<D>(Type.ACTION_SEND, backId, id, data)
	
	class ActionResponse<D>(
		backId: Int,
		id: Int,
		data: D,
		val sendId: Int
	) : ActionInteraction<D>(Type.ACTION_RESPONSE, backId, id, data)
	
	class ActionRepair<D>(
		backId: Int
	) : Action<D>(Type.ACTION_REPAIR, backId)
}
