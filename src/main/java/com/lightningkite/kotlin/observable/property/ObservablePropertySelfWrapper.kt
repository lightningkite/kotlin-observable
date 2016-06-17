package com.lightningkite.kotlin.observable.property

import com.lightningkite.kotlin.Disposable

/**
 * Created by jivie on 4/5/16.
 */
class ObservablePropertySelfWrapper<T : ObservableProperty<T>>(initValue: T) : ObservablePropertyBase<T>(), Disposable {
    val myListener: (T) -> Unit = {
        super.update(it)
    }

    init {
        initValue.add(myListener)
    }

    override var value: T = initValue
        get() = value
        set(value) {
            field.remove(myListener)
            field = value
            field.add(myListener)
        }

    override fun dispose() {
        value.remove(myListener)
    }
}