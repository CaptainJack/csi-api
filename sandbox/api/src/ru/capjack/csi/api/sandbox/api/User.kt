package ru.capjack.csi.api.sandbox.api

open class User(
	val id: Long,
	val name: String?,
	val rank: Rank = Rank.JUNIOR
) {
	
	enum class Rank {
		JUNIOR,
		MAJOR,
		SENIOR
	}
}
