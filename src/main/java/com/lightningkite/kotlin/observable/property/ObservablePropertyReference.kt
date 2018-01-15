package com.lightningkite.kotlin.observable.property

import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KMutableProperty1

/**
 * Creates an observable property out of a reference to a property.
 * Note that for the observable to update, you *must* modify the reference through this observable.
 * Created by jivie on 2/22/16.
 */
class ObservablePropertyReference<T>(val getterFun: () -> T, val setterFun: (T) -> Unit) : BaseObservableProperty<T>() {

    override var value: T
        get() = getterFun()
        set(value) {
            setterFun(value)
            update()
        }
}

/**
 * Creates an observable property out of a reference to a property.
 * Note that for the observable to update, you *must* modify the reference through this observable.
 */
fun <T> KMutableProperty0<T>.toObservablePropertyReference(): ObservablePropertyReference<T> {
    return ObservablePropertyReference({ get() }, { set(it) })
}

/**
 * Creates an observable property out of a reference to a property.
 * Note that for the observable to update, you *must* modify the reference through this observable.
 */
fun <T, R> KMutableProperty1<R, T>.toObservablePropertyReference(receiver: R): ObservablePropertyReference<T> {
    return ObservablePropertyReference({ get(receiver) }, { set(receiver, it) })
}