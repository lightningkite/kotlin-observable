package com.ivieleague.kotlin.observable

/**
 * Created by jivie on 4/7/16.
 */
abstract class ObservablePropertySelf<T> : ObservablePropertyBase<T>() {
    override var value: T
        get() = this as T
        set(value) {
            throw IllegalAccessException()
        }
}