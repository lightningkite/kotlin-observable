package com.lightningkite.kotlin.observable.property

import java.util.*

/**
 * A simple basis for an [ObservableProperty].
 * Created by jivie on 2/22/16.
 */
abstract class BaseObservableProperty<T>() : MutableObservableProperty<T> {

    @Transient private val list = ArrayList<(T) -> Unit>()

    override fun remove(element: (T) -> Unit): Boolean = list.remove(element)
    override fun add(element: (T) -> Unit): Boolean = list.add(element)

    open fun update() {
        for (listener in list) {
            listener(value)
        }
    }
}