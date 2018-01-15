package com.lightningkite.kotlin.observable.property

/**
 * A standard observable property.
 * Simply is a box for a value that can be read or set.
 * Upon being set, it will call every listener it is given.
 *
 * Created by jivie on 1/19/16.
 */
open class StandardObservableProperty<T>(
        initValue: T
) : BaseObservableProperty<T>() {

    override var value: T = initValue
        set(value) {
            field = value
            update()
        }
}