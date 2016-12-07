package com.lightningkite.kotlin.observable.list

import com.lightningkite.kotlin.Disposable
import com.lightningkite.kotlin.lifecycle.LifecycleConnectable
import com.lightningkite.kotlin.lifecycle.LifecycleListener
import com.lightningkite.kotlin.runAll
import java.util.*

/**
 * Allows you to observe the changes to a list.
 * Created by josep on 9/7/2015.
 */
class ObservableListMultiGroupingBy<E, G, L>(
        val source: ObservableList<E>,
        val grouper: (E) -> Collection<G>,
        val listWrapper: (ObservableList<E>) -> L,
        val innerList: ObservableListWrapper<Pair<G, L>> = observableListOf()
) : ObservableList<Pair<G, L>> by innerList, Disposable {

    private inner class InnerList() : ObservableListIndicies<E>(source) {
    }

    private val groupLists = HashMap<G, InnerList>()

    private fun getOrMakeGroup(group: G): InnerList {
        val current = groupLists[group]
        if (current != null) return current

        val list = InnerList()
        val wrapper = listWrapper(list)
        innerList.add(group to wrapper)
        groupLists[group] = list
        return list
    }

    private fun removeGroup(group: G) {
        groupLists.remove(group)
        val index = innerList.indexOfFirst { it.first == group }
        if (index != -1) {
            innerList.removeAt(index)
        }
    }

    private fun reset() {
        innerList.clear()
        groupLists.clear()

        for (index in source.indices) {
            val item = source[index]
            val groups = grouper(item)
            for (group in groups) {
                val current = getOrMakeGroup(group)
                current.indexList.add(index)
                current.onAdd.runAll(item, current.indexList.size - 1)
                current.onUpdate.runAll(current)
            }
        }
    }

    fun modifyIndicesBy(after: Int, by: Int) {
        for ((group, inner) in groupLists) {
            for (indexIndex in inner.indexList.indices) {
                val index = inner.indexList[indexIndex]
                if (index >= after)
                    inner.indexList[indexIndex] = index + by
            }
        }
    }

    fun getCurrentIndexGroup(index: Int): Collection<G> {
        val result = ArrayList<G>()
        for ((group, list) in groupLists) {
            if (list.indexList.contains(index)) {
                result.add(group)
            }
        }
        return result
    }

    val listener = ObservableListListenerSet<E>(
            onAddListener = { item, index ->
                modifyIndicesBy(index, 1)
                val groups = grouper(item)
                for (group in groups) {
                    val list = getOrMakeGroup(group)
                    list.indexList.add(index)
                    list.onAdd.runAll(item, list.indexList.size - 1)
                    list.onUpdate.runAll(list)
                }
            },
            onRemoveListener = { item, index ->
                val groups = getCurrentIndexGroup(index)
                for (group in groups) {
                    val groupList = groupLists[group]
                    if (groupList != null) {
                        val indexIndex = groupList.indexList.indexOf(index)
                        groupList.indexList.removeAt(indexIndex)
                        modifyIndicesBy(index, -1)
                        groupList.onRemove.runAll(item, indexIndex)
                        groupList.onUpdate.runAll(groupList)
                        if (groupList.isEmpty()) {
                            removeGroup(group)
                        }
                    } else throw IllegalArgumentException()
                }
            },
            onChangeListener = { oldItem, item, index ->
                val oldGroups = getCurrentIndexGroup(index).toSet()
                val newGroups = grouper(item).toSet()
                val addingGroups = newGroups.minus(oldGroups)
                val removingGroups = oldGroups.minus(newGroups)
                val sameGroups = newGroups.union(oldGroups)
                for (group in sameGroups) {
                    val groupList = groupLists[group]
                    if (groupList != null) {
                        val indexIndex = groupList.indexList.indexOf(index)
                        groupList.onChange.runAll(oldItem, item, indexIndex)
                        groupList.onUpdate.runAll(groupList)
                    } else throw IllegalArgumentException()
                }
                for (group in removingGroups) {
                    val oldGroupList = groupLists[group] ?: throw IllegalArgumentException()
                    val oldIndexIndex = oldGroupList.indexList.indexOf(index)
                    oldGroupList.indexList.removeAt(oldIndexIndex)
                    oldGroupList.onRemove.runAll(oldItem, oldIndexIndex)
                    oldGroupList.onUpdate.runAll(oldGroupList)
                    if (oldGroupList.isEmpty()) {
                        removeGroup(group)
                    }
                }
                for (group in addingGroups) {
                    val newGroupList = getOrMakeGroup(group)
                    newGroupList.indexList.add(index)
                    newGroupList.onAdd.runAll(item, newGroupList.indexList.size - 1)
                    newGroupList.onUpdate.runAll(newGroupList)
                }
            },
            onMoveListener = { item, oldIndex, index ->
                val groups = getCurrentIndexGroup(oldIndex)
                for (group in groups) {
                    val groupList = groupLists[group]
                    if (groupList != null) {
                        val indexIndex = groupList.indexList.indexOf(oldIndex)
                        modifyIndicesBy(oldIndex, -1)
                        modifyIndicesBy(index, 1)
                        groupList.indexList[indexIndex] = index
                    } else throw IllegalArgumentException()
                }
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

fun <E, G, L> ObservableList<E>.multiGroupingBy(
        grouper: (E) -> Collection<G>,
        listWrapper: (ObservableList<E>) -> L
) = ObservableListMultiGroupingBy(this, grouper, listWrapper)

fun <E, G, L> ObservableList<E>.multiGroupingBy(
        lifecycle: LifecycleConnectable,
        grouper: (E) -> Collection<G>,
        listWrapper: (ObservableList<E>) -> L
): ObservableListMultiGroupingBy<E, G, L> {
    val list = ObservableListMultiGroupingBy(this, grouper, listWrapper)
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

inline fun <E, G> ObservableList<E>.multiGroupingBy(noinline grouper: (E) -> Collection<G>) = multiGroupingBy(grouper, { it })

inline fun <E, G> ObservableList<E>.multiGroupingBy(lifecycle: LifecycleConnectable, noinline grouper: (E) -> Collection<G>) = multiGroupingBy(lifecycle, grouper, { it })