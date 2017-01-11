//TODO: Make this to replace the old sub operation

//package com.lightningkite.kotlin.observable.property
//
//import kotlin.reflect.KMutableProperty1
//
///**
// * Created by jivie on 2/22/16.
// */
//class ObservablePropertySubObservable<A, B>(
//        val owningObservable: ObservableProperty<A>,
//        val getter: (A)->MutableObservableProperty<B>
//) : EnablingMutableCollection<(B) -> Unit>(), MutableObservableProperty<B> {
//    val myListener: (T) -> Unit = {
//        super.update(it)
//    }
//
//
//
//    private var observable: ObservableProperty<B> = owningObservable.value.let(getter)
//        set(value) {
//            field.remove(myListener)
//            field = value
//            field.add(myListener)
//            super.update(value.value)
//        }
//    init {
//        observable.add(myListener)
//    }
//
//    override fun dispose() {
//        observable.remove(myListener)
//    }
//
//    override var value: T
//        get() = observable.value
//        set(value) {
//            val obs = observable
//            if (obs is MutableObservableProperty) {
//                obs.value = value
//            } else {
//                throw IllegalAccessException()
//            }
//        }
//
//    val callback = { a: A -> update() }
//    override fun enable() {
//        observable.add(callback)
//    }
//
//    override fun disable() {
//        observable.remove(callback)
//    }
//}
//
//fun <A, B> ObservableProperty<A>.sub(getterFun: (A) -> B)
//        = ObservablePropertySubReference(this, getterFun, { a, b -> throw IllegalAccessException("This is read only.") })
//
//fun <A, B> ObservableProperty<A>.sub(getterFun: (A) -> B, setterFun: (A, B) -> Unit)
//        = ObservablePropertySubReference(this, getterFun, setterFun)
//
//fun <A, B> ObservableProperty<A>.sub(property: KMutableProperty1<A, B>)
//        = ObservablePropertySubReference(this, { property.get(it) }, { a, b -> property.set(a, b) })