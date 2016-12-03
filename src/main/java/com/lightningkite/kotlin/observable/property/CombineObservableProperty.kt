package com.lightningkite.kotlin.observable.property

/**
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