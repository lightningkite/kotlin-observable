package com.lightningkite.kotlin.observable.property

import java.util.*

/**
 *
 * Created by joseph on 12/2/16.
 */
class CombineObservableProperty2<A, B, T>(
        val observableA: ObservableProperty<A>,
        val observableB: ObservableProperty<B>,
        val combine: (A, B) -> T
) : EnablingMutableCollection<(T) -> Unit>(), ObservableProperty<T> {

    override var value = combine(observableA.value, observableB.value)

    override fun update() {
        value = combine(observableA.value, observableB.value)
        super.update()
    }

    val callbackA = { item: A ->
        update()
    }
    val callbackB = { item: B ->
        update()
    }

    override fun enable() {
        observableA.add(callbackA)
        observableB.add(callbackB)
        update()
    }

    override fun disable() {
        observableA.remove(callbackA)
        observableB.remove(callbackB)
    }
}

class CombineObservableProperty3<A, B, C, T>(
        val observableA: ObservableProperty<A>,
        val observableB: ObservableProperty<B>,
        val observableC: ObservableProperty<C>,
        val combine: (A, B, C) -> T
) : EnablingMutableCollection<(T) -> Unit>(), ObservableProperty<T> {

    override var value = combine(observableA.value, observableB.value, observableC.value)

    override fun update() {
        value = combine(observableA.value, observableB.value, observableC.value)
        super.update()
    }

    val callbackA = { item: A ->
        update()
    }
    val callbackB = { item: B ->
        update()
    }
    val callbackC = { item: C ->
        update()
    }

    override fun enable() {
        observableA.add(callbackA)
        observableB.add(callbackB)
        observableC.add(callbackC)
    }

    override fun disable() {
        observableA.remove(callbackA)
        observableB.remove(callbackB)
        observableC.remove(callbackC)
    }
}

@Suppress("UNCHECKED_CAST")
class CombineObservablePropertyBlind<T>(
        val observables: Collection<ObservableProperty<*>>,
        val combine: () -> T
) : EnablingMutableCollection<(T) -> Unit>(), ObservableProperty<T> {

    constructor(vararg observables: ObservableProperty<*>, combine: () -> T) : this(observables.toList(), combine)

    override var value = combine()

    override fun update() {
        value = combine()
        super.update()
    }

    val callbacks = HashMap<ObservableProperty<Any?>, (Any?) -> Unit>()

    override fun enable() {
        callbacks += observables.map {
            val newListener = { _: Any? -> update() }
            val itFake = it as ObservableProperty<Any?>
            itFake.add(newListener)
            itFake to newListener
        }
    }

    override fun disable() {
        callbacks.forEach { (key, value) -> key.remove(value) }
        callbacks.clear()
    }
}