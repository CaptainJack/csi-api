package ru.capjack.sandbox.csi.api.data

sealed class SealedClass {
	class SubClass(var a: Int): SealedClass()
	
	object SubObject: SealedClass()
	
	sealed class SubSealedClass: SealedClass() {
		class SubSubClass(val a: Int): SubSealedClass()
		
		object SubSubObject: SubSealedClass()
	}
}