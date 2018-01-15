package com.lightningkite.kotlin.observable.property

/**
 * Created by jivie on 4/5/16.
 */
class ObservableObservablePropertyOpt<T>(initialObservable: MutableObservableProperty<T>? = null) : BaseObservableProperty<T?>(), Disposable {
    val myListener: (T) -> Unit = {
        super.update(it)
    }

    var observable: ObservableProperty<T>? = null
        set(value) {
            field?.remove(myListener)
            field = value
            field?.add(myListener)
            super.update(value?.value)
        }

    init {
        observable = initialObservable
    }

    override var value: T?
        get() = observable?.value
        set(value) {
            val obs = observable
            if (obs is MutableObservableProperty && value != null) {
                obs.value = value
            } else {
                throw IllegalAccessException()
            }
        }

    override fun dispose() {
        observable?.remove(myListener)
    }

}