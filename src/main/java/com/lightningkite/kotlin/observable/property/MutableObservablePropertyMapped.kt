package com.lightningkite.kotlin.observable.property

import com.lightningkite.kotlin.lambda.invokeAll

/**
 * Transforms an observable property from one type to another.
 * Created by jivie on 2/22/16.
 */
class MutableObservablePropertyMapped<S, T>(
        val observable: MutableObservableProperty<S>,
        val transformer: (S) -> T,
        val reverseTransformer: (T) -> S
) : EnablingMutableCollection<(T) -> Unit>(), MutableObservableProperty<T> {
    override var value: T
        get() = transformer(observable.value)
        set(value) {
            observable.value = reverseTransformer(value)
        }

    val callback = { a: S -> invokeAll(transformer(observable.value)) }
    override fun enable() {
        observable.add(callback)
    }

    override fun disable() {
        observable.remove(callback)
    }
}

fun <S, T> MutableObservableProperty<S>.transform(mapper: (S) -> T, reverseMapper: (T) -> S): MutableObservablePropertyMapped<S, T> {
    return MutableObservablePropertyMapped(this, mapper, reverseMapper)
}