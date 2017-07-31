package com.lightningkite.kotlin.observable.property

import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate
import kotlin.reflect.KMutableProperty1

class ObservablePropertySubReference<A, B>(
        val observable: ObservableProperty<A>,
        val getterFun: (A) -> B,
        val setterFun: (A, B) -> Unit
) : EnablingMutableCollection<(B) -> Unit>(), MutableObservableProperty<B> {
    override var value: B
        get() = getterFun(observable.value)
        set(value) {
            setterFun(observable.value, value)
            observable.update()
        }

    val callback = { a: A -> update() }
    override fun enable() {
        observable.add(callback)
    }

    override fun disable() {
        observable.remove(callback)
    }


    override fun spliterator(): Spliterator<(B) -> Unit> {
        return super<EnablingMutableCollection>.spliterator()
    }

    override fun removeIf(filter: Predicate<in (B) -> Unit>): Boolean {
        return super<EnablingMutableCollection>.removeIf(filter)
    }

    override fun forEach(action: Consumer<in (B) -> Unit>) {
        super<EnablingMutableCollection>.forEach(action)
    }
}

fun <A, B> ObservableProperty<A>.sub(getterFun: (A) -> B)
        = ObservablePropertySubReference(this, getterFun, { a, b -> throw IllegalAccessException("This is read only.") })

fun <A, B> ObservableProperty<A>.sub(getterFun: (A) -> B, setterFun: (A, B) -> Unit)
        = ObservablePropertySubReference(this, getterFun, setterFun)

fun <A, B> ObservableProperty<A>.sub(property: KMutableProperty1<A, B>)
        = ObservablePropertySubReference(this, { property.get(it) }, { a, b -> property.set(a, b) })