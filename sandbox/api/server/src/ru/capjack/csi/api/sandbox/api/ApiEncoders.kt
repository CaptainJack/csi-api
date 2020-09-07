package ru.capjack.csi.api.sandbox.api

import ru.capjack.tool.io.biser.Encoder

internal object ApiEncoders{
	val ENTITY_SessionUser: Encoder<SessionUser> = {
		writeInt(1)
		writeLong(it.id)
		writeStringNullable(it.name)
		writeLong(it.coins)
	}
	
	val ENTITY_User: Encoder<User> = {
		when (it) {
			is SessionUser -> {
				ENTITY_SessionUser(it)
			}
			else -> {
				writeInt(2)
				writeLong(it.id)
				writeStringNullable(it.name)
				write(it.rank, ENUM_User_Rank)
			}
		}
	}
	
	val ENUM_User_Rank: Encoder<User.Rank> = {
		writeInt(when (it) {
			User.Rank.JUNIOR -> 0
			User.Rank.MAJOR -> 1
			User.Rank.SENIOR -> 2
		})
	}
	
}
