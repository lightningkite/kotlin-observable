package com.ivieleague.kotlin.observable.list

import com.ivieleague.kotlin.lifecycle.LifecycleConnectable
import com.ivieleague.kotlin.lifecycle.LifecycleListener
import com.ivieleague.kotlin.observable.property.bind

/**
 * Extensions that allow using ObservablePropertys with the LifecycleConnectable.
 * Created by jivie on 6/1/16.
 */

fun <T> LifecycleConnectable.bind(observable: ObservableList<T>, listener: (ObservableList<T>) -> Unit) {
    bind(observable.onUpdate, listener)
}

fun <T> LifecycleConnectable.bind(observable: ObservableList<T>, listenerSet: ObservableListListenerSet<T>) {
    connect(object : LifecycleListener {
        override fun onStart() {
            observable.addListenerSet(listenerSet)
        }

        override fun onStop() {
            observable.removeListenerSet(listenerSet)
        }
    })
}