package com.lightningkite.kotlin.observable.property

import com.lightningkite.kotlin.lambda.invokeAll

/**
 * Transforms an observable to observe another observable.
 * Created by jivie on 2/22/16.
 */
class ObservablePropertySubObservable<A, B>(
        val owningObservable: ObservableProperty<A>,
        val getter: (A) -> ObservableProperty<B>
) : EnablingMutableCollection<(B) -> Unit>(), MutableObservableProperty<B> {

    var currentSub: ObservableProperty<B>? = null

    override var value: B
        get() = owningObservable.value.let(getter).value
        set(value) {
            val currentSub = owningObservable.value.let(getter)
            if (currentSub is MutableObservableProperty<B>) {
                currentSub.value = value
            } else throw IllegalAccessException("ObservableProperty is not mutable")
        }

    val outerCallback = { a: A ->
        invokeAll(value)
        resub()
    }
    val innerCallback = { b: B -> invokeAll(value) }

    override fun enable() {
        owningObservable.add(outerCallback)
        resub()
    }

    override fun disable() {
        owningObservable.remove(outerCallback)
        unsub()
    }

    private fun resub() {
        unsub()
        val sub = owningObservable.value.let(getter)
        sub.add(innerCallback)
        currentSub = sub
    }

    private fun unsub() {
        currentSub?.remove(innerCallback)
        currentSub = null
    }
}

/**
 * Transforms an observable to observe an observable within the observable.
 * Trippy, right?
 */
fun <A, B> ObservableProperty<A>.subObs(getterFun: (A) -> ObservableProperty<B>) = ObservablePropertySubObservable(this, getterFun)