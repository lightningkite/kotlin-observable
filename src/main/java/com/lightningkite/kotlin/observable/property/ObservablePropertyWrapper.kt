package com.lightningkite.kotlin.observable.property

/**
 * Created by jivie on 4/5/16.
 */
class ObservablePropertyWrapper<T>(val getter: () -> T, collection: MutableCollection<(T) -> Unit>) : ObservableProperty<T>, MutableCollection<(T) -> Unit> by collection {
    override val value: T
        get() = getter()
}