package com.lightningkite.kotlin.observable.property

/**
 *
 * Created by josep on 8/19/2017.
 */
class VirtualMutableObservableProperty<T>(
        val getterFun: () -> T,
        val setterFun: (T) -> Unit,
        val event: MutableCollection<(T) -> Unit>
) : EnablingMutableCollection<(T) -> Unit>(), MutableObservableProperty<T> {

    override var value: T
        get() = getterFun()
        set(value) {
            setterFun(value)
        }

    val listener = { t: T -> update() }

    override fun enable() {
        event.add(listener)
    }

    override fun disable() {
        event.remove(listener)
    }
}
