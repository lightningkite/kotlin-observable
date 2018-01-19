package com.lightningkite.kotlin.observable.property

import kotlin.reflect.KProperty

/**
 * A property that can be observed.
 * Created by josep on 1/28/2016.
 */
interface ObservableProperty<T> {

    /**
     * The current value of the observable property.
     */
    val value: T

    /**
     * Simply retrieves the value.
     * Used for property delegates.
     */
    operator fun getValue(thisRef: Any?, prop: KProperty<*>): T {
        return value
    }

    /**
     * Adds a listener to be notified when the value changes.
     */
    fun add(element: (T) -> Unit): Boolean

    /**
     * Removes a listener
     */
    fun remove(element: (T) -> Unit): Boolean
}