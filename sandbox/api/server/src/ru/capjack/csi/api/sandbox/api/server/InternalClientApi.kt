package ru.capjack.csi.api.sandbox.api.server

import ru.capjack.csi.api.OuterApi
import ru.capjack.csi.api.sandbox.api.client.ClientApi

interface InternalClientApi : ClientApi, OuterApi
