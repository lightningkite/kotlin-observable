package com.ivieleague.kotlin.observable.property

import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KMutableProperty1

/**
 * Created by jivie on 2/22/16.
 */
class ObservablePropertyReference<T>(val getterFun: () -> T, val setterFun: (T) -> Unit) : ObservablePropertyBase<T>() {

    override var value: T
        get() = getterFun()
        set(value) {
            setterFun(value)
        }
}

fun <T> KMutableProperty0<T>.toKObservableReference(): ObservablePropertyReference<T> {
    return ObservablePropertyReference({ get() }, { set(it) })
}

fun <T, R> KMutableProperty1<R, T>.toKObservableReference(receiver: R): ObservablePropertyReference<T> {
    return ObservablePropertyReference({ get(receiver) }, { set(receiver, it) })
}