package ru.capjack.csi.api

expect class OuterSubscriptionReadyDelegate(subscription: OuterSubscription) {
	
	fun delay(fn: () -> Unit)
	
	fun ready()
}