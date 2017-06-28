package com.lightningkite.kotlin.observable.list

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by joseph on 9/26/16.
 */
class ObservableListGroupingByTest {

    fun makeTestLists(): Triple<ObservableListWrapper<String>, ObservableListGroupingBy<String, Char?, ObservableListSorted<String>>, ObservableListFlatMapping<Pair<Char?, ObservableListSorted<String>>, String>> {
        val core = observableListOf("a1", "a2", "a3", "c1", "d1", "c2", "c3", "d2", "d3")
        val grouped = core.groupingBy({ it.firstOrNull() }, { it.sorting { a, b -> a < b } })
        val flattened = grouped.flatMapping { it.second }
        return Triple(core, grouped, flattened)
    }

    fun makeSortingTestLists(): Triple<ObservableListWrapper<String>, ObservableListGroupingBy<String, Char?, ObservableListSorted<String>>, ObservableListFlatMapping<Pair<Char?, ObservableListSorted<String>>, String>> {
        val core = observableListOf("a1", "a2", "a3", "c1", "d1", "c2", "c3", "d2", "d3")
        val grouped = core.groupingBy({ it.firstOrNull() }, { it.sorting { a, b -> a < b } })
        val flattened = grouped.sorting { a, b -> a.first ?: 'z' < b.first ?: 'z' }.flatMapping { it.second }
        return Triple(core, grouped, flattened)
    }

    @Test
    fun printOnlyTest() {
        val (core, grouped, flattened) = makeSortingTestLists()
        flattened.onAdd += { item, index ->
            println("added index $index")
            println("added item $item")
        }
        println("model: " + core.joinToString())
        println("grouped: " + grouped.joinToString { it.first.toString() + ": " + it.second.joinToString() })
        println("flattened: " + flattened.joinToString())
        core.add("c4")
        println("model: " + core.joinToString())
        println("grouped: " + grouped.joinToString { it.first.toString() + ": " + it.second.joinToString() })
        println("flattened: " + flattened.joinToString())

    }

    @Test
    fun addCore1() {
        var callbackOccurred = false
        val (core, grouped, flattened) = makeSortingTestLists()
        flattened.onAdd += { item, index ->
            assertEquals(3, index)
            assertEquals(item, "a4")
            callbackOccurred = true
        }
        println("model: " + core.joinToString())
        println("grouped: " + grouped.joinToString { it.first.toString() + ": " + it.second.joinToString() })
        println("flattened: " + flattened.joinToString())
        core.add("a4")
        println("model: " + core.joinToString())
        println("grouped: " + grouped.joinToString { it.first.toString() + ": " + it.second.joinToString() })
        println("flattened: " + flattened.joinToString())

        assert(callbackOccurred, { "callback didn't occur" })
    }

    @Test
    fun addCore2() {
        var callbackOccurred = false
        val (core, grouped, flattened) = makeSortingTestLists()
        flattened.onAdd += { item, index ->
            assertEquals(6, index)
            assertEquals(item, "c4")
            callbackOccurred = true
        }
        println("model: " + core.joinToString())
        println("grouped: " + grouped.joinToString { it.first.toString() + ": " + it.second.joinToString() })
        println("flattened: " + flattened.joinToString())
        core.add("c4")
        println("model: " + core.joinToString())
        println("grouped: " + grouped.joinToString { it.first.toString() + ": " + it.second.joinToString() })
        println("flattened: " + flattened.joinToString())

        assert(callbackOccurred, { "callback didn't occur" })
    }

    @Test
    fun newGroupHasNew() {
        val new = "e1"
        var callbackOccurred = false
        val (core, grouped, flattened) = makeSortingTestLists()
        grouped.onAdd += { list, index ->
            println(index)
            assert(list.second.contains(new))
            callbackOccurred = true
        }
        println("model: " + core.joinToString())
        println("grouped: " + grouped.joinToString { it.first.toString() + ": " + it.second.joinToString() })
        println("flattened: " + flattened.joinToString())
        core.add(new)
        println("model: " + core.joinToString())
        println("grouped: " + grouped.joinToString { it.first.toString() + ": " + it.second.joinToString() })
        println("flattened: " + flattened.joinToString())

        assert(callbackOccurred, { "callback didn't occur" })
    }

    @Test
    fun addNewGroup1() {
        var callbackOccurred = false
        val (core, grouped, flattened) = makeSortingTestLists()
        flattened.onAdd += { item, index ->
            println(index)
            assertEquals(item, "e1")
            callbackOccurred = true
        }
        println("model: " + core.joinToString())
        println("grouped: " + grouped.joinToString { it.first.toString() + ": " + it.second.joinToString() })
        println("flattened: " + flattened.joinToString())
        core.add("e1")
        println("model: " + core.joinToString())
        println("grouped: " + grouped.joinToString { it.first.toString() + ": " + it.second.joinToString() })
        println("flattened: " + flattened.joinToString())

        assert(callbackOccurred, { "callback didn't occur" })
    }

    @Test
    fun addNewGroup2() {
        val (core, grouped, flattened) = makeSortingTestLists()
        var callbackOccurred = false
        flattened.onAdd += { item, index ->
            println(index)
            assertEquals(item, "b1")
            callbackOccurred = true
        }
        println("model: " + core.joinToString())
        println("grouped: " + grouped.joinToString { it.first.toString() + ": " + it.second.joinToString() })
        println("flattened: " + flattened.joinToString())
        core.add("b1")
        println("model: " + core.joinToString())
        println("grouped: " + grouped.joinToString { it.first.toString() + ": " + it.second.joinToString() })
        println("flattened: " + flattened.joinToString())

        assert(callbackOccurred, { "callback didn't occur" })
    }

    @Test
    fun changeNewGroup() {
        val (core, grouped, flattened) = makeSortingTestLists()
        println("model: " + core.joinToString())
        println("grouped: " + grouped.joinToString { it.first.toString() + ": " + it.second.joinToString() })
        println("flattened: " + flattened.joinToString())
        core[2] = "b1"
        println("model: " + core.joinToString())
        println("grouped: " + grouped.joinToString { it.first.toString() + ": " + it.second.joinToString() })
        println("flattened: " + flattened.joinToString())
        assert(flattened.indexOf("b1") != -1)
    }

    @Test
    fun changeInternal() {
        val (core, grouped, flattened) = makeSortingTestLists()
        println("model: " + core.joinToString())
        println("grouped: " + grouped.joinToString { it.first.toString() + ": " + it.second.joinToString() })
        println("flattened: " + flattened.joinToString())

        var callbackOccurred = false
        grouped[0].second.onChange += { oldItem, item, index ->
            println("index $index, item $oldItem -> $item")
            assertEquals(item, "a4")
            callbackOccurred = true
        }

        core[2] = "a4"
        println("model: " + core.joinToString())
        println("grouped: " + grouped.joinToString { it.first.toString() + ": " + it.second.joinToString() })
        println("flattened: " + flattened.joinToString())

        assert(callbackOccurred, { "callback didn't occur" })
    }

    @Test
    fun move() {
        val (core, grouped, flattened) = makeSortingTestLists()
        println("model: " + core.joinToString())
        println("grouped: " + grouped.joinToString { it.first.toString() + ": " + it.second.joinToString() })
        println("flattened: " + flattened.joinToString())
        core.move(0, 5)
        println("model: " + core.joinToString())
        println("grouped: " + grouped.joinToString { it.first.toString() + ": " + it.second.joinToString() })
        println("flattened: " + flattened.joinToString())
    }

    @Test
    fun remove() {
        val (core, grouped, flattened) = makeSortingTestLists()
        println("model: " + core.joinToString())
        println("grouped: " + grouped.joinToString { it.first.toString() + ": " + it.second.joinToString() })
        println("flattened: " + flattened.joinToString())
        core.removeAt(4)
        println("model: " + core.joinToString())
        println("grouped: " + grouped.joinToString { it.first.toString() + ": " + it.second.joinToString() })
        println("flattened: " + flattened.joinToString())
    }

    @Test
    fun removeGroup() {
        val (core, grouped, flattened) = makeSortingTestLists()
        val startGroups = grouped.size
        println("model: " + core.joinToString())
        println("grouped: " + grouped.joinToString { it.first.toString() + ": " + it.second.joinToString() })
        println("flattened: " + flattened.joinToString())
        core.removeAt(0)
        core.removeAt(0)
        core.removeAt(0)
        println("model: " + core.joinToString())
        println("grouped: " + grouped.joinToString { it.first.toString() + ": " + it.second.joinToString() })
        println("flattened: " + flattened.joinToString())
        assertEquals(startGroups - 1, grouped.size)

        core.add("a3")
        println("model: " + core.joinToString())
        println("grouped: " + grouped.joinToString { it.first.toString() + ": " + it.second.joinToString() })
        println("flattened: " + flattened.joinToString())
    }

    @Test
    fun death() {
        val core = observableListOf("a1", "b1", "c1", "d1", "e1")
        val grouped = core.groupingBy({ it.firstOrNull() }, { it.sorting { a, b -> a < b } })
        val flattened = grouped.sorting { a, b -> a.first ?: 'z' < b.first ?: 'z' }.flatMapping { it.second }

        println("model: " + core.joinToString())
        println("grouped: " + grouped.joinToString { it.first.toString() + ": " + it.second.joinToString() })
        println("flattened: " + flattened.joinToString())
        core.removeAt(0)
        core.removeAt(0)
        core.removeAt(0)
        println("model: " + core.joinToString())
        println("grouped: " + grouped.joinToString { it.first.toString() + ": " + it.second.joinToString() })
        println("flattened: " + flattened.joinToString())

        core.add("a3")
        println("model: " + core.joinToString())
        println("grouped: " + grouped.joinToString { it.first.toString() + ": " + it.second.joinToString() })
        println("flattened: " + flattened.joinToString())
    }
}