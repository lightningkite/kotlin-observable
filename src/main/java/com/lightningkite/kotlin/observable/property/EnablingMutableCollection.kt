package com.lightningkite.kotlin.observable.property

import java.util.*

/**
 * A collection that calls [enable] when the collection has an element in it, and [disable] when the collection is empty.
 * Created by joseph on 12/2/16.
 */
abstract class EnablingMutableCollection<E>() : ArrayList<E>() {

    abstract fun enable(): Unit
    abstract fun disable(): Unit

    var active = false
    fun checkUp() {
        if (!super.isEmpty() && !active) {
            active = true
            enable()
        }
    }

    fun checkDown() {
        if (super.isEmpty() && active) {
            active = false
            disable()
        }
    }

    override fun add(element: E): Boolean {
        val result = super.add(element)
        checkUp()
        return result
    }

    override fun addAll(elements: Collection<E>): Boolean {
        val result = super.addAll(elements)
        checkUp()
        return result
    }

    override fun clear() {
        super.clear()
        checkDown()
    }

    override fun remove(element: E): Boolean {
        val result = super.remove(element)
        checkDown()
        return result
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        val result = super.removeAll(elements)
        checkDown()
        return result
    }

    override fun retainAll(elements: Collection<E>): Boolean {
        val result = super.retainAll(elements)
        checkDown()
        return result
    }
}