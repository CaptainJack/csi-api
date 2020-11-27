package ru.capjack.csi.api.sandbox.api

import ru.capjack.csi.api.sandbox.api.client.SessionService

interface ClientApi {
	val session: SessionService
}

