package com.lightningkite.kotlin.observable.property

import com.lightningkite.kotlin.Disposable

/**
 * Created by jivie on 4/5/16.
 */
class ObservablePropertySelfWrapperOpt<T : ObservableProperty<T>>(initValue: T?) : ObservablePropertyBase<T?>(), Disposable {
    val myListener: (T) -> Unit = {
        super.update(it)
    }

    override var value: T? = null
        get() = value
        set(value) {
            field?.remove(myListener)
            field = value
            field?.add(myListener)
        }

    init {
        value = initValue
    }

    override fun dispose() {
        value?.remove(myListener)
    }
}