package com.lightningkite.kotlin.observable.property

import com.lightningkite.kotlin.lambda.invokeAll

/**
 * An observable property from other observable properties, reduced into a single on via a given function, [reduce].
 * Created by joseph on 12/2/16.
 */
class ReducingObservableProperty<E, T>(
        val observables: List<ObservableProperty<E>>,
        val reduce: (List<E>) -> T
) : EnablingMutableCollection<(T) -> Unit>(), ObservableProperty<T> {

    override var value = reduce(observables.map { it.value })
    fun update() {
        value = reduce(observables.map { it.value })
        invokeAll(value)
    }

    val callback = { item: E ->
        update()
    }

    override fun enable() {
        for (observable in observables) {
            observable.add(callback)
        }
    }

    override fun disable() {
        for (observable in observables) {
            observable.remove(callback)
        }
    }
}

/**
 * Creates an observable property from many, transforming the value via the given function, [reduce].
 */
fun <T, E> List<ObservableProperty<E>>.reducing(reduce: (List<E>) -> T): ObservableProperty<T> = ReducingObservableProperty(this, reduce)