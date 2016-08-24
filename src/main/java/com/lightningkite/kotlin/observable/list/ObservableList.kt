package com.lightningkite.kotlin.observable.list

import com.lightningkite.kotlin.observable.property.ObservableProperty

/**
 * Allows you to observe the changes to a list.
 * Created by josep on 9/7/2015.
 */
interface ObservableList<E> : MutableList<E> {

    val onAdd: MutableSet<(E, Int) -> Unit>
    val onChange: MutableSet<(E, Int) -> Unit>
    val onUpdate: ObservableProperty<ObservableList<E>>
    val onReplace: MutableSet<(ObservableList<E>) -> Unit>
    val onRemove: MutableSet<(E, Int) -> Unit>

    fun replace(list: List<E>)
    fun updateAt(index: Int) {
        this[index] = this[index]
    }

    fun update(element: E): Boolean {
        val index = indexOf(element)
        if (index != -1)
            updateAt(index)
        return index != -1
    }
}