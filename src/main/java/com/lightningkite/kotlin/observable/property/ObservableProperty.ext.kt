package com.lightningkite.kotlin.observable.property

import com.lightningkite.kotlin.lifecycle.LifecycleConnectable
import com.lightningkite.kotlin.observable.list.ObservableList
import com.lightningkite.kotlin.observable.list.ObservableListWrapper

/**
 * Created by jivie on 6/16/16.
 */

fun <A> ObservableProperty<A>.addAndInvoke(lambda: (A) -> Unit) {
    add(lambda)
    lambda.invoke(value)
}

@Deprecated("Use the version of sub without a lifecycle.")
fun <A, B> ObservableProperty<A>.sub(lifecycle: LifecycleConnectable, mapper: (A) -> ObservableProperty<B>): ObservableObservableProperty<B> {
    val obs = ObservableObservableProperty(mapper(value))
    lifecycle.bind(this) {
        obs.observable = mapper(it)
    }
    return obs
}

@Deprecated("Use the version of sub without a lifecycle.")
fun <A, B> ObservableProperty<A?>.subOpt(lifecycle: LifecycleConnectable, mapper: (A?) -> ObservableProperty<B>?): ObservableObservablePropertyOpt<B> {
    val obs = ObservableObservablePropertyOpt<B>()
    obs.observable = mapper(value)
    lifecycle.bind(this) {
        if (it != null)
            obs.observable = mapper(it)
        else
            obs.observable = null
    }
    return obs
}

fun <A, B> ObservableProperty<A>.subObs(getterFun: (A) -> ObservableProperty<B>) {

}

fun <A> ObservableProperty<List<A>>.toObservableList(lifecycle: LifecycleConnectable): ObservableList<A> {
    val list = ObservableListWrapper<A>()
    lifecycle.bind(this) {
        list.replace(it)
    }
    return list
}