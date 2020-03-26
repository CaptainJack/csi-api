package ru.capjack.csi.api.sandbox.api

import ru.capjack.tool.io.biser.Encoder
import ru.capjack.csi.api.sandbox.api.User.Rank

internal object ApiEncoders{
	val ENTITY_SessionUser: Encoder<SessionUser> = {
		writeLong(it.id)
		writeString(it.name)
		writeLong(it.coins)
	}
	
	val ENTITY_User: Encoder<User> = {
		when (it) {
			is SessionUser -> {
				writeInt(1)
				ENTITY_SessionUser(it)
			}
			else -> {
				writeInt(2)
				writeLong(it.id)
				writeString(it.name)
				write(it.rank, ENUM_User_Rank)
			}
		}
	}
	
	val ENUM_User_Rank: Encoder<Rank> = {
		writeInt(when (it) {
			Rank.JUNIOR -> 1
			Rank.MAJOR -> 2
			Rank.SENIOR -> 3
		})
	}
	
}
