package com.lightningkite.kotlin.observable.property

import kotlin.reflect.KProperty

/**
 * Created by joseph on 12/2/16.
 */
class ConstantObservableProperty<T>(override val value: T) : ObservableProperty<T> {
    override val size: Int
        get() = 0

    override fun contains(element: (T) -> Unit): Boolean = false
    override fun containsAll(elements: Collection<(T) -> Unit>): Boolean = false
    override fun isEmpty(): Boolean = true
    override fun add(element: (T) -> Unit): Boolean = false
    override fun addAll(elements: Collection<(T) -> Unit>): Boolean = false
    override fun clear() {
    }

    override fun iterator(): MutableIterator<(T) -> Unit> = object : MutableIterator<(T) -> Unit> {
        override fun hasNext(): Boolean = false
        override fun next(): (T) -> Unit = throw UnsupportedOperationException("not implemented")
        override fun remove() {
        }
    }

    override fun remove(element: (T) -> Unit): Boolean = false
    override fun removeAll(elements: Collection<(T) -> Unit>): Boolean = false
    override fun retainAll(elements: Collection<(T) -> Unit>): Boolean = false
    override fun getValue(thisRef: Any?, prop: KProperty<*>): T {
        return super.getValue(thisRef, prop)
    }
}