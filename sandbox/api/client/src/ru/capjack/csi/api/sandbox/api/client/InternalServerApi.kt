package ru.capjack.csi.api.sandbox.api.client

import ru.capjack.csi.api.OuterApi
import ru.capjack.csi.api.sandbox.api.server.ServerApi

interface InternalServerApi : ServerApi, OuterApi
