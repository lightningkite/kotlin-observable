package com.lightningkite.kotlin.observable.list

import com.lightningkite.kotlin.observable.property.ObservableProperty

/**
 * Allows you to observe the changes to a list.
 * Created by josep on 9/7/2015.
 */
interface ObservableList<E> : MutableList<E> {

    val onAdd: MutableCollection<(E, Int) -> Unit>
    val onChange: MutableCollection<(E, E, Int) -> Unit>
    val onMove: MutableCollection<(E, Int, Int) -> Unit>
    val onUpdate: ObservableProperty<ObservableList<E>>
    val onReplace: MutableCollection<(ObservableList<E>) -> Unit>
    val onRemove: MutableCollection<(E, Int) -> Unit>

    fun move(fromIndex: Int, toIndex: Int)
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