package com.lightningkite.kotlin.observable.property

import kotlin.reflect.KProperty

/**
 * An observable property that can be modified.
 * Created by josep on 5/31/2016.
 */
interface MutableObservableProperty<T> : ObservableProperty<T> {
    override var value: T

    operator fun setValue(thisRef: Any?, prop: KProperty<*>, v: T) {
        value = v
    }
}