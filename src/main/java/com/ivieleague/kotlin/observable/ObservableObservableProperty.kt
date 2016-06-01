package com.ivieleague.kotlin.observable

import com.ivieleague.kotlin.Disposable

/**
 * Created by jivie on 4/5/16.
 */
class ObservableObservableProperty<T>(initialObservable: MutableObservableProperty<T>) : ObservablePropertyBase<T>(), Disposable {
    val myListener: (T) -> Unit = {
        super.update(it)
    }


    init {
        initialObservable.add(myListener)
    }

    var observable: MutableObservableProperty<T> = initialObservable
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
            observable.value = value
        }
}