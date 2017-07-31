package com.lightningkite.kotlin.observable.list

import com.lightningkite.kotlin.Disposable
import com.lightningkite.kotlin.invokeAll
import com.lightningkite.kotlin.lifecycle.LifecycleConnectable
import com.lightningkite.kotlin.lifecycle.LifecycleListener
import com.lightningkite.kotlin.runAll
import java.util.*

/**
 * Allows you to observe the changes to a list.
 * Created by josep on 9/7/2015.
 */
class ObservableListGroupingBy<E, G, L>(
        val source: ObservableList<E>,
        val grouper: (E) -> G,
        val listWrapper: (ObservableList<E>) -> L,
        val innerList: ObservableListWrapper<Pair<G, L>> = observableListOf()
) : ObservableList<Pair<G, L>> by innerList, Disposable {

    private inner class InnerList() : ObservableListIndicies<E>(source)

    private val groups = HashMap<G, InnerList>()

    private fun getOrMakeGroup(group: G): InnerList = getOrMakeGroup(group, {})

    private inline fun getOrMakeGroup(group: G, crossinline modify: (InnerList) -> Unit): InnerList {
        val current = groups[group]
        if (current != null) return current.apply(modify)

        val list = InnerList().apply(modify)
        val wrapper = listWrapper(list)
        innerList.add(group to wrapper)
        groups[group] = list
        return list
    }

    private fun removeGroup(group: G) {
        groups.remove(group)
        val index = innerList.indexOfFirst { it.first == group }
        if (index != -1) {
            innerList.removeAt(index)
        }
    }

    private fun reset() {
        innerList.clear()
        groups.clear()

        for (index in source.indices) {
            val item = source[index]
            val group = grouper(item)
            getOrMakeGroup(group) {
                it.indexList.add(index)
                it.onAdd.runAll(item, it.indexList.size - 1)
                it.onUpdate.invokeAll(it)
            }
        }
    }

    fun modifyIndicesBy(after: Int, by: Int) {
        for ((group, inner) in groups) {
            for (indexIndex in inner.indexList.indices) {
                val index = inner.indexList[indexIndex]
                if (index >= after)
                    inner.indexList[indexIndex] = index + by
            }
        }
    }

    fun getCurrentIndexGroup(index: Int): G {
        for ((group, list) in groups) {
            if (list.indexList.contains(index)) {
                return group
            }
        }
        throw IllegalStateException()
    }

    val listener = ObservableListListenerSet<E>(
            onAddListener = { item, index ->
                modifyIndicesBy(index, 1)
                val group = grouper(item)
                getOrMakeGroup(group) {
                    it.indexList.add(index)
                    it.onAdd.runAll(item, it.indexList.size - 1)
                    it.onUpdate.invokeAll(it)
                }
            },
            onRemoveListener = { item, index ->
                val group = getCurrentIndexGroup(index)
                val groupList = groups[group]
                if (groupList != null) {
                    val indexIndex = groupList.indexList.indexOf(index)
                    groupList.indexList.removeAt(indexIndex)
                    modifyIndicesBy(index, -1)
                    groupList.onRemove.runAll(item, indexIndex)
                    groupList.onUpdate.invokeAll(groupList)
                    if (groupList.isEmpty()) {
                        removeGroup(group)
                    }
                } else throw IllegalArgumentException()
            },
            onChangeListener = { oldItem, item, index ->
                val oldGroup = getCurrentIndexGroup(index)
                val newGroup = grouper(item)
                if (oldGroup == newGroup) {
                    val groupList = groups[oldGroup]
                    if (groupList != null) {
                        val indexIndex = groupList.indexList.indexOf(index)
                        groupList.onChange.runAll(oldItem, item, indexIndex)
                        groupList.onUpdate.invokeAll(groupList)
                    } else throw IllegalArgumentException()
                } else {
                    val oldGroupList = groups[oldGroup] ?: throw IllegalArgumentException()
                    val oldIndexIndex = oldGroupList.indexList.indexOf(index)
                    oldGroupList.indexList.removeAt(oldIndexIndex)
                    oldGroupList.onRemove.runAll(oldItem, oldIndexIndex)
                    oldGroupList.onUpdate.invokeAll(oldGroupList)
                    if (oldGroupList.isEmpty()) {
                        removeGroup(oldGroup)
                    }

                    getOrMakeGroup(newGroup) {
                        it.indexList.add(index)
                        it.onAdd.runAll(item, it.indexList.size - 1)
                        it.onUpdate.invokeAll(it)
                    }
                }
            },
            onMoveListener = { item, oldIndex, index ->
                val group = getCurrentIndexGroup(oldIndex)
                val groupList = groups[group]
                if (groupList != null) {
                    val indexIndex = groupList.indexList.indexOf(oldIndex)
                    modifyIndicesBy(oldIndex, -1)
                    modifyIndicesBy(index, 1)
                    groupList.indexList[indexIndex] = index
                } else throw IllegalArgumentException()
            },
            onReplaceListener = { list ->
                reset()
            }
    )
//
//    private fun updateLists() {
//        val newCategories = source.map(grouper).toSet()
//        val oldCategories = innerList.map { it.first }.toSet()
//        for (category in newCategories) {
//            if (category !in oldCategories) {
//                val filtering = InnerList()
//                val wrapped = listWrapper(filtering)
//                innerLists[wrapped] = filtering
//                innerList.add(category to wrapped)
//            }
//        }
//        for (category in oldCategories) {
//            if (category !in newCategories) {
//                innerList.removeAll() { it.first == category }
//            }
//        }
//    }

    init {
        setup()
    }

    fun setup() {
        reset()
        source.addListenerSet(listener)
    }

    override fun dispose() {
        source.removeListenerSet(listener)
        innerList.clear()
    }
}

fun <E, G, L> ObservableList<E>.groupingBy(
        grouper: (E) -> G,
        listWrapper: (ObservableList<E>) -> L
) = ObservableListGroupingBy(this, grouper, listWrapper)

fun <E, G, L> ObservableList<E>.groupingBy(
        lifecycle: LifecycleConnectable,
        grouper: (E) -> G,
        listWrapper: (ObservableList<E>) -> L
): ObservableListGroupingBy<E, G, L> {
    val list = ObservableListGroupingBy(this, grouper, listWrapper)
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

inline fun <E, G> ObservableList<E>.groupingBy(noinline grouper: (E) -> G) = groupingBy(grouper, { it })

inline fun <E, G> ObservableList<E>.groupingBy(lifecycle: LifecycleConnectable, noinline grouper: (E) -> G) = groupingBy(lifecycle, grouper, { it })