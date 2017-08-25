package com.lightningkite.kotlin.observable.property

class VirtualObservableProperty<T>(
        val getterFun: () -> T,
        val event: MutableCollection<(T) -> Unit>
) : EnablingMutableCollection<(T) -> Unit>(), ObservableProperty<T> {

    override val value: T
        get() = getterFun()

    val listener = { t: T -> update() }

    override fun enable() {
        event.add(listener)
    }

    override fun disable() {
        event.remove(listener)
    }
}