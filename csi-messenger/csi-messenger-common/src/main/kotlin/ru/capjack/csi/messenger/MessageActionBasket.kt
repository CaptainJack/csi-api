package ru.capjack.csi.messenger

class MessageActionBasket<D>() {
	private val messages: MutableList<Message.ActionInteraction<D>> = mutableListOf()
	
	fun add(message: Message.ActionInteraction<D>) {
		messages.add(message)
	}
	
	fun clearTo(id: Int) {
		val each = messages.iterator()
		while (each.hasNext()) {
			val mid = each.next().id
			if (mid <= id) {
				each.remove()
				if (mid == id) {
					break
				}
			}
		}
		
		if (messages.isNotEmpty() && id < INT_MIN_2) {
			clearTo(Int.MAX_VALUE)
		}
	}
	
	companion object {
		private const val INT_MIN_2 = Int.MIN_VALUE / 2
	}
}
