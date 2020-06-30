package ru.capjack.csi.api.sandbox.api.client

import ru.capjack.csi.api.log
import ru.capjack.csi.api.logS
import ru.capjack.csi.api.sandbox.api.SessionUser
import ru.capjack.csi.api.sandbox.api.User

internal val LOG_ENTITY_SessionUser: StringBuilder.(SessionUser) -> Unit = {
	append('{')
	logS("id", it.id)
	logS("name", it.name)
	log("coins", it.coins)
	append('}')
}

internal val LOG_ENTITY_User: StringBuilder.(User) -> Unit = {
	when (it) {
		is SessionUser -> LOG_ENTITY_SessionUser(it)
		else -> {
			append('{')
			logS("id", it.id)
			logS("name", it.name)
			log("rank", it.rank.name)
			append('}')
		}
	}
}

