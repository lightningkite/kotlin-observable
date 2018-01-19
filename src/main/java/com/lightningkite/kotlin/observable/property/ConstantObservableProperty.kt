package com.lightningkite.kotlin.observable.property

/**
 * A constant observable property - the value never changes.
 * Created by joseph on 12/2/16.
 */
class ConstantObservableProperty<T>(override val value: T) : ObservableProperty<T> {
    override fun add(element: (T) -> Unit): Boolean = false
    override fun remove(element: (T) -> Unit): Boolean = false
}