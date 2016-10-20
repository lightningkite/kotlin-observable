package com.lightningkite.kotlin.observable.list

import com.lightningkite.kotlin.Disposable
import com.lightningkite.kotlin.lifecycle.LifecycleConnectable
import com.lightningkite.kotlin.lifecycle.LifecycleListener
import java.util.*

/**
 * Allows you to observe the changes to a list.
 * Created by josep on 9/7/2015.
 */
class ObservableListGroupingBy<E, G, L>(
        val source: ObservableList<E>,
        val grouper: (E) -> G,
        val listWrapper: (ObservableListFiltered<E>) -> L,
        val innerList: ObservableListWrapper<Pair<G, L>> = observableListOf()
) : ObservableList<Pair<G, L>> by innerList, Disposable {

    val filteringLists = HashMap<L, ObservableListFiltered<E>>()
    val listener = ObservableListListenerSet<E>(
            onAddListener = { item, index ->
                updateLists()
            },
            onRemoveListener = { item, index ->
                updateLists()
            },
            onChangeListener = { oldItem, item, index ->
                updateLists()
            },
            onMoveListener = { item, oldIndex, index -> },
            onReplaceListener = { list ->
                updateLists()
            }
    )

    private fun updateLists() {
        val newCategories = source.map(grouper).toSet()
        val oldCategories = innerList.map { it.first }.toSet()
        for (category in newCategories) {
            if (category !in oldCategories) {
                val filtering = source.filtering { grouper(it) == category }
                val wrapped = listWrapper(filtering)
                filteringLists[wrapped] = filtering
                innerList.add(category to wrapped)
            }
        }
        for (category in oldCategories) {
            if (category !in newCategories) {
                innerList.removeAll {
                    if (it.first == category) {
                        val toDispose = filteringLists.remove(it.second)
                        toDispose?.dispose()
                        true
                    } else false
                }
            }
        }
    }

    init {
        setup()
    }

    fun setup() {
        updateLists()
    }

    override fun dispose() {
        for (item in innerList) {
            val toDispose = filteringLists.remove(item.second)
            toDispose?.dispose()
        }
        innerList.clear()
    }
}

fun <E, G, L> ObservableList<E>.groupingBy(
        grouper: (E) -> G,
        listWrapper: (ObservableListFiltered<E>) -> L
) = ObservableListGroupingBy(this, grouper, listWrapper)

fun <E, G, L> ObservableList<E>.groupingBy(
        lifecycle: LifecycleConnectable,
        grouper: (E) -> G,
        listWrapper: (ObservableListFiltered<E>) -> L
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