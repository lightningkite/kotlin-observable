package com.lightningkite.kotlin.observable.property

import com.lightningkite.kotlin.lifecycle.LifecycleConnectable
import com.lightningkite.kotlin.lifecycle.LifecycleListener

/**
 * Extensions that allow using ObservablePropertys with the LifecycleConnectable.
 * Created by jivie on 6/1/16.
 */

fun <T> LifecycleConnectable.bind(observable: ObservableProperty<T>, listener: (T) -> Unit) {
    connect(object : LifecycleListener {
        override fun onStart() {
            observable.add(listener)
            listener(observable.value)
        }

        override fun onStop() {
            observable.remove(listener)
        }
    })
}

fun <A, B> LifecycleConnectable.bind(
        observableA: ObservableProperty<A>,
        observableB: ObservableProperty<B>,
        action: (A, B) -> Unit
) {
    connect(object : LifecycleListener {

        val itemA = { item: A -> action(item, observableB.value) }
        val itemB = { item: B -> action(observableA.value, item) }

        override fun onStart() {
            observableA.add(itemA)
            observableB.add(itemB)
            action(observableA.value, observableB.value)
        }

        override fun onStop() {
            observableA.remove(itemA)
            observableB.remove(itemB)
        }
    })
}

fun LifecycleConnectable.bindBlind(
        vararg observables: ObservableProperty<out Any?>,
        action: () -> Unit
) {
    connect(object : LifecycleListener {

        val item = { item: Any? -> action() }

        override fun onStart() {
            for (obs in observables) {
                obs.add(item)
            }
            action()
        }

        override fun onStop() {
            for (obs in observables) {
                obs.remove(item)
            }
        }
    })
}

fun <A, B, C> LifecycleConnectable.bind(
        observableA: ObservableProperty<A>,
        observableB: ObservableProperty<B>,
        observableC: ObservableProperty<C>,
        action: (A, B, C) -> Unit
) {
    connect(object : LifecycleListener {

        val itemA = { item: A -> action(item, observableB.value, observableC.value) }
        val itemB = { item: B -> action(observableA.value, item, observableC.value) }
        val itemC = { item: C -> action(observableA.value, observableB.value, item) }

        override fun onStart() {
            observableA.add(itemA)
            observableB.add(itemB)
            observableC.add(itemC)
            action(observableA.value, observableB.value, observableC.value)
        }

        override fun onStop() {
            observableA.remove(itemA)
            observableB.remove(itemB)
            observableC.remove(itemC)
        }
    })
}

fun <A, B, C, D> LifecycleConnectable.bind(
        observableA: ObservableProperty<A>,
        observableB: ObservableProperty<B>,
        observableC: ObservableProperty<C>,
        observableD: ObservableProperty<D>,
        action: (A, B, C, D) -> Unit
) {
    connect(object : LifecycleListener {

        val itemA = { item: A -> action(item, observableB.value, observableC.value, observableD.value) }
        val itemB = { item: B -> action(observableA.value, item, observableC.value, observableD.value) }
        val itemC = { item: C -> action(observableA.value, observableB.value, item, observableD.value) }
        val itemD = { item: D -> action(observableA.value, observableB.value, observableC.value, item) }

        override fun onStart() {
            observableA.add(itemA)
            observableB.add(itemB)
            observableC.add(itemC)
            observableD.add(itemD)
            action(observableA.value, observableB.value, observableC.value, observableD.value)
        }

        override fun onStop() {
            observableA.remove(itemA)
            observableB.remove(itemB)
            observableC.remove(itemC)
            observableD.remove(itemD)
        }
    })
}

inline fun <A, T> LifecycleConnectable.bindSub(observable: ObservableProperty<A>, crossinline mapper: (A) -> ObservableProperty<T>, noinline action: (T) -> Unit) {
    val obs = ObservableObservableProperty(mapper(observable.value))
    bind(observable) {
        obs.observable = mapper(it)
    }
    bind(obs, action)
}