package com.lightningkite.kotlin.observable.list

import com.lightningkite.kotlin.Disposable
import com.lightningkite.kotlin.collection.mapping
import com.lightningkite.kotlin.lifecycle.LifecycleConnectable
import com.lightningkite.kotlin.lifecycle.LifecycleListener
import com.lightningkite.kotlin.observable.property.ObservableProperty
import com.lightningkite.kotlin.observable.property.ObservablePropertyReference
import com.lightningkite.kotlin.runAll
import java.util.*

/**
 * Created by jivie on 5/23/16.
 */
class ObservableListSorted<E>(val source: ObservableList<E>, val getInsertionIndex: (List<E>, E) -> Int) : ObservableList<E>, Disposable {

    val indexList = ArrayList<Int>()
    val indexListMapped = indexList.mapping<Int, E>(
            { it: Int -> source[it] },
            { it: E -> indexList.indexOf(source.indexOf(it)) }
    )

    fun indexGetInsertionIndex(item: E) = getInsertionIndex(indexListMapped, item)
    var listenerSet: ObservableListListenerSet<E> = ObservableListListenerSet(
            onAddListener = { item, index ->
                for (i in indexList.indices) {
                    if (indexList[i] >= index)
                        indexList[i]++
                }
                val sortedIndex = indexGetInsertionIndex(item)
                indexList.add(sortedIndex, index)
                onAdd.runAll(item, sortedIndex)
                onUpdate.runAll(this)
            },
            onRemoveListener = { item, index ->
                val sortedIndex = indexList.indexOf(index)
                if (sortedIndex != -1) {
                    indexList.removeAt(sortedIndex)
                    for (i in indexList.indices) {
                        if (indexList[i] >= index)
                            indexList[i]--
                    }
                    onRemove.runAll(item, sortedIndex)
                    onUpdate.runAll(this)
                }
            },
            onMoveListener = { item, oldIndex, index ->
                //Do nothing.  We don't care what order it is; we're sorting it!
            },
            onChangeListener = { old, item, index ->
                val removeSortedIndex = indexList.indexOf(index)
                indexList.removeAt(removeSortedIndex)
                val sortedIndex = indexGetInsertionIndex(item)

                if (removeSortedIndex != sortedIndex) {
                    onRemove.runAll(old, removeSortedIndex)
                    indexList.add(sortedIndex, index)
                    onAdd.runAll(item, sortedIndex)
                    onUpdate.runAll(this)
                } else {
                    indexList.add(sortedIndex, index)
                    onChange.runAll(old, item, removeSortedIndex)
                    onUpdate.runAll(this)
                }
            },
            onReplaceListener = {
                indexList.clear()
                it.forEachIndexed { index, item ->
                    val sortedIndex = indexGetInsertionIndex(item)
                    indexList.add(sortedIndex, index)
                }
                onReplace.runAll(this)
                onUpdate.runAll(this)
            }
    )

    init {
        setup()
    }

    var connected = false
    fun setup() {
        if (connected) return
        indexList.clear()
        source.forEachIndexed { index, item ->
            val sortedIndex = indexGetInsertionIndex(item)
            indexList.add(sortedIndex, index)
        }
        source.addListenerSet(listenerSet)
        connected = true
    }

    override fun dispose() {
        if (!connected) return
        source.removeListenerSet(listenerSet)
        connected = false
    }

    override val size: Int get() = indexList.size

    override fun contains(element: E): Boolean = source.contains(element)
    override fun containsAll(elements: Collection<E>): Boolean = source.containsAll(elements)
    override fun get(index: Int): E = source[indexList[index]] ?: throw IllegalStateException("No source specified.")
    override fun indexOf(element: E): Int = indexList.indexOf(source.indexOf(element))
    override fun lastIndexOf(element: E): Int = indexList.indexOf(source.lastIndexOf(element))
    override fun isEmpty(): Boolean = indexList.isEmpty()
    override fun add(element: E): Boolean = source.add(element)
    override fun add(index: Int, element: E) = source.add(index, element)
    override fun addAll(index: Int, elements: Collection<E>): Boolean = source.addAll(index, elements)
    override fun addAll(elements: Collection<E>): Boolean = source.addAll(elements)
    override fun clear() = source.clear()
    override fun remove(element: E): Boolean = source.remove(element)
    override fun removeAll(elements: Collection<E>): Boolean = source.removeAll(elements)
    override fun removeAt(index: Int): E = source.removeAt(indexList[index])
    override fun retainAll(elements: Collection<E>): Boolean = source.retainAll(elements)
    override fun set(index: Int, element: E): E = source.set(indexList[index], element)
    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> = indexList.subList(fromIndex, toIndex).map { source[it] }.toMutableList()
    override fun move(fromIndex: Int, toIndex: Int) {
        throw UnsupportedOperationException("You can't rearrange items in a sorted list.")
    }

    override fun listIterator(): MutableListIterator<E> = throw UnsupportedOperationException()
    override fun listIterator(index: Int): MutableListIterator<E> = throw UnsupportedOperationException()
    override fun iterator(): MutableIterator<E> = indexList.iterator().mapping({ source[it] }, { indexOf(it) })
    override fun replace(list: List<E>) = source.replace(list)

    val listenerMapper = { input: (E, Int) -> Unit ->
        { element: E, index: Int ->
            input(element, indexList.indexOf(index))
        }
    }

    override val onAdd = HashSet<(E, Int) -> Unit>()
    override val onChange = HashSet<(E, E, Int) -> Unit>()
    override val onMove = HashSet<(E, Int, Int) -> Unit>()
    override val onUpdate: ObservableProperty<ObservableList<E>> = ObservablePropertyReference<ObservableList<E>>({ this@ObservableListSorted }, { replace(it) })
    override val onReplace = HashSet<(ObservableList<E>) -> Unit>()
    override val onRemove = HashSet<(E, Int) -> Unit>()

//    override val onAdd: MutableSet<(E, Int) -> Unit> get() = source.onAdd.map(listenerMapper)
//    override val onRemove: MutableSet<(E, Int) -> Unit> get() = source.onRemove.map(listenerMapper)
//    override val onChange: MutableSet<(E, Int) -> Unit> get() = source.onChange.map(listenerMapper)
//
//    override val onUpdate = sourceInit.onUpdate.mapObservable<KObservableListInterface<E>, KObservableListInterface<E>>({ it -> this }, { throw IllegalAccessException() })
//    override val onReplace: MutableSet<(KObservableListInterface<E>) -> Unit> get() = source.onReplace.map({ input -> { input(this) } })

}

@Deprecated("This has been renamed to 'sorting' because it sorts on the fly.", ReplaceWith("sorting(sorter)", "com.lightningkite.kotlin.observable.list.sorting"))
inline fun <E> ObservableList<E>.sorted(noinline sorter: (E, E) -> Boolean): ObservableListSorted<E> = sorting(sorter)

inline fun <E> ObservableList<E>.sorting(noinline sorter: (E, E) -> Boolean): ObservableListSorted<E>
        = ObservableListSorted(this, { list, item ->
    if (list.isEmpty())
        0
    else {
        val res = list.indexOfFirst { sorter(item, it) }
        if (res == -1) list.size else res
    }
})

inline fun <E> ObservableList<E>.sorting(lifecycle: LifecycleConnectable, noinline sorter: (E, E) -> Boolean): ObservableListSorted<E> {
    val list = ObservableListSorted(this, { list, item ->
        if (list.isEmpty())
            0
        else {
            val res = list.indexOfFirst { sorter(item, it) }
            if (res == -1) list.size else res
        }
    })
    lifecycle.connect(object : LifecycleListener {
        override fun onStart() {
            list.setup()
        }

        override fun onStop() {
            list.dispose()
        }
    })
    return list
}

@Deprecated("This has been renamed to 'sortingWithInsertionIndex' because it sorts on the fly.", ReplaceWith("sortingWithInsertionIndex(getInsertionIndex)", "com.lightningkite.kotlin.observable.list.sortingWithInsertionIndex"))
inline fun <E> ObservableList<E>.sortedWithInsertionIndex(noinline getInsertionIndex: (List<E>, E) -> Int): ObservableListSorted<E>
        = ObservableListSorted(this, getInsertionIndex)

inline fun <E> ObservableList<E>.sortingWithInsertionIndex(noinline getInsertionIndex: (List<E>, E) -> Int): ObservableListSorted<E>
        = ObservableListSorted(this, getInsertionIndex)

inline fun <E> ObservableList<E>.sortingWithInsertionIndex(lifecycle: LifecycleConnectable, noinline getInsertionIndex: (List<E>, E) -> Int): ObservableListSorted<E> {
    val list = ObservableListSorted(this, getInsertionIndex)
    lifecycle.connect(object : LifecycleListener {
        override fun onStart() {
            list.setup()
        }

        override fun onStop() {
            list.dispose()
        }
    })
    return list
}