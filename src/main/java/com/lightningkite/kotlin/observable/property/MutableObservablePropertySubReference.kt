package com.lightningkite.kotlin.observable.property

import com.lightningkite.kotlin.lambda.invokeAll
import kotlin.reflect.KMutableProperty1

/**
 * Transforms an observable to observe a subvalue.
 */
class MutableObservablePropertySubReference<A, B>(
        val observable: MutableObservableProperty<A>,
        val getterFun: (A) -> B,
        val setterFun: (A, B) -> Unit
) : EnablingMutableCollection<(B) -> Unit>(), MutableObservableProperty<B> {
    override var value: B
        get() = getterFun(observable.value)
        set(value) {
            setterFun(observable.value, value)
            observable.value = observable.value
        }

    val callback = { a: A -> invokeAll(getterFun(observable.value)) }
    override fun enable() {
        observable.add(callback)
    }

    override fun disable() {
        observable.remove(callback)
    }
}

fun <A, B> MutableObservableProperty<A>.sub(getterFun: (A) -> B, setterFun: (A, B) -> Unit)
        = MutableObservablePropertySubReference(this, getterFun, setterFun)

fun <A, B> MutableObservableProperty<A>.sub(property: KMutableProperty1<A, B>)
        = MutableObservablePropertySubReference(this, { property.get(it) }, { a, b -> property.set(a, b) })