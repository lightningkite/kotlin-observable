package com.lightningkite.kotlin.observable.property

/**
 * Created by joseph on 12/2/16.
 */
class ReducingObservableProperty<E, T>(
        val observables: List<ObservableProperty<E>>,
        val reduce: (List<E>) -> T
) : EnablingMutableCollection<(T) -> Unit>(), ObservableProperty<T> {

    override var value = reduce(observables.map { it.value })
    override fun update() {
        value = reduce(observables.map { it.value })
        super.update()
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

fun <T, E> List<ObservableProperty<E>>.reducing(reduce: (List<E>) -> T): ObservableProperty<T> = ReducingObservableProperty(this, reduce)