package com.lightningkite.kotlin.observable.property

import com.lightningkite.kotlin.lifecycle.LifecycleConnectable
import com.lightningkite.kotlin.observable.list.ObservableList
import com.lightningkite.kotlin.observable.list.ObservableListWrapper

operator fun <T> ObservableProperty<T>.plusAssign(lambda: (T) -> Unit): Unit {
    add(lambda)
}

operator fun <T> ObservableProperty<T>.minusAssign(lambda: (T) -> Unit): Unit {
    remove(lambda)
}

fun <A> ObservableProperty<A>.addAndInvoke(lambda: (A) -> Unit) {
    add(lambda)
    lambda.invoke(value)
}

fun <A> ObservableProperty<List<A>>.toObservableList(lifecycle: LifecycleConnectable): ObservableList<A> {
    val list = ObservableListWrapper<A>()
    lifecycle.bind(this) {
        list.replace(it)
    }
    return list
}