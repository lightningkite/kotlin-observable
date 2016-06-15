package com.lightningkite.kotlin.observable.property

import com.lightningkite.kotlin.runAll
import kotlin.reflect.KProperty

/**
 * Created by josep on 1/28/2016.
 */
interface ObservableProperty<T> : MutableCollection<(T) -> Unit> {

    /**
     * The current value of the observable property.
     */
    val value: T

    operator fun getValue(thisRef: Any?, prop: KProperty<*>): T {
        return value
    }

    fun update() = this.runAll(value)
}