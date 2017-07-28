package com.lightningkite.kotlin.observable.property

import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate

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

    override fun spliterator(): Spliterator<(T) -> Unit> {
        return super<EnablingMutableCollection>.spliterator()
    }

    override fun removeIf(filter: Predicate<in (T) -> Unit>): Boolean {
        return super<EnablingMutableCollection>.removeIf(filter)
    }

    override fun forEach(action: Consumer<in (T) -> Unit>) {
        super<EnablingMutableCollection>.forEach(action)
    }
}

fun <T, E> List<ObservableProperty<E>>.reducing(reduce: (List<E>) -> T): ObservableProperty<T> = ReducingObservableProperty(this, reduce)