package ru.capjack.csi.api.sandbox.api

import ru.capjack.tool.io.biser.Decoder
import ru.capjack.tool.io.biser.Decoders
import ru.capjack.tool.io.biser.UnknownIdDecoderException

internal object ApiDecoders{
	val ENTITY_SessionUser: Decoder<SessionUser> = {
		SessionUser(
			readLong(),
			readString(),
			readLong()
		)
	}
	
	private val ENTITY_User_RAW: Decoder<User> = {
		User(
			readLong(),
			readString(),
			read(ENUM_User_Rank)
		)
	}
	
	val ENTITY_User: Decoder<User> = {
		when (val id = readInt()) {
			1 -> ENTITY_SessionUser()
			2 -> ENTITY_User_RAW()
			else -> throw UnknownIdDecoderException(id, User::class)
		}
	}
	
	val ENUM_User_Rank: Decoder<User.Rank> = {
		when (val id = readInt()) {
			0 -> User.Rank.JUNIOR
			1 -> User.Rank.MAJOR
			2 -> User.Rank.SENIOR
			else -> throw UnknownIdDecoderException(id, User.Rank::class)
		}
	}
	
}
