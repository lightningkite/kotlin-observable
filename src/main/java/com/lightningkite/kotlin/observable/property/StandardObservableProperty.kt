package com.lightningkite.kotlin.observable.property

/**
 * Created by jivie on 1/19/16.
 */
open class StandardObservableProperty<T>(
        initValue: T
) : ObservablePropertyBase<T>() {

    override var value: T = initValue
        set(value) {
            field = value
            update(value)
        }
}