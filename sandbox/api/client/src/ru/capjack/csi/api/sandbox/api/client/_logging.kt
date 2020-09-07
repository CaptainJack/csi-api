package ru.capjack.csi.api.sandbox.api.client

import ru.capjack.csi.api.log
import ru.capjack.csi.api.logS
import ru.capjack.csi.api.sandbox.api.SessionUser
import ru.capjack.csi.api.sandbox.api.User

internal val LOG_ENTITY_SessionUser: StringBuilder.(SessionUser) -> Unit = {
	append('{')
	logS("id", it.id)
	logS("name", it.name, LOG_NULLABLE_STRING)
	log("coins", it.coins)
	append('}')
}

internal val LOG_NULLABLE_STRING: StringBuilder.(String?) -> Unit = {
	if (it == null) append("NULL") else log(it, LOG_STRING)
}

internal val LOG_ENTITY_User: StringBuilder.(User) -> Unit = {
	when (it) {
		is SessionUser -> LOG_ENTITY_SessionUser(it)
		else -> {
			append('{')
			logS("id", it.id)
			logS("name", it.name, LOG_NULLABLE_STRING)
			log("rank", it.rank.name)
			append('}')
		}
	}
}

internal val LOG_STRING: StringBuilder.(String) -> Unit = {
	log(it)
}

