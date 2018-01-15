package com.lightningkite.kotlin.observable.property

import com.lightningkite.kotlin.lambda.invokeAll

/**
 * Transforms an observable to observe a subvalue.
 */
class ObservablePropertySubReference<A, B>(
        val observable: ObservableProperty<A>,
        val getterFun: (A) -> B
) : EnablingMutableCollection<(B) -> Unit>(), ObservableProperty<B> {
    override val value: B
        get() = getterFun(observable.value)

    val callback = { a: A -> invokeAll(getterFun(observable.value)) }
    override fun enable() {
        observable.add(callback)
    }

    override fun disable() {
        observable.remove(callback)
    }
}

fun <A, B> ObservableProperty<A>.sub(getterFun: (A) -> B)
        = ObservablePropertySubReference(this, getterFun)