package com.lightningkite.kotlin.observable.property

import com.lightningkite.kotlin.Disposable

/**
 * Created by jivie on 4/5/16.
 */
class ObservableObservableProperty<T>(initialObservable: ObservableProperty<T>) : ObservablePropertyBase<T>(), Disposable {
    val myListener: (T) -> Unit = {
        super.update(it)
    }


    init {
        initialObservable.add(myListener)
    }

    var observable: ObservableProperty<T> = initialObservable
        set(value) {
            field.remove(myListener)
            field = value
            field.add(myListener)
            super.update(value.value)
        }

    override fun dispose() {
        observable.remove(myListener)
    }

    override var value: T
        get() = observable.value
        set(value) {
            val obs = observable
            if (obs is MutableObservableProperty) {
                obs.value = value
            } else {
                throw IllegalAccessException()
            }
        }
}