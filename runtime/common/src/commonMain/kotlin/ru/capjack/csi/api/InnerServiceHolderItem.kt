package ru.capjack.csi.api

class InnerServiceHolderItem<S: Any>(val instance: ServiceInstance<S>, val delegate: InnerServiceDelegate<S>)