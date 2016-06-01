package com.ivieleague.kotlin.observable.list

/**
 * Created by jivie on 5/5/16.
 */

class ObservableListListenerSet<T>(
        val onAddListener: (item: T, position: Int) -> Unit,
        val onRemoveListener: (item: T, position: Int) -> Unit,
        val onChangeListener: (item: T, position: Int) -> Unit,
        val onReplaceListener: (list: ObservableList<T>) -> Unit
) {
}

inline fun <T> ObservableList<T>.addListenerSet(set: ObservableListListenerSet<T>) {
    onAdd.add(set.onAddListener)
    onRemove.add(set.onRemoveListener)
    onChange.add(set.onChangeListener)
    onReplace.add(set.onReplaceListener)
}

inline fun <T> ObservableList<T>.removeListenerSet(set: ObservableListListenerSet<T>) {
    onAdd.remove(set.onAddListener)
    onRemove.remove(set.onRemoveListener)
    onChange.remove(set.onChangeListener)
    onReplace.remove(set.onReplaceListener)
}