package com.lightningkite.kotlin.observable.list

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

/**
 * Created by joseph on 9/26/16.
 */
class ChainTest {
    val newElement: Char = 'q'

    class TestData(
            val label: String,
            val source: ObservableList<Char>,
            val transformed: ObservableList<Char>
    )

    inline fun makeTestData(label: String, transforms: ObservableList<Char>.() -> ObservableList<Char>): TestData {
        val copy = observableListOf('b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p')
        return TestData(label, copy, copy.transforms())
    }

    fun makeTestDatas(): List<TestData> {
        return listOf(
                makeTestData("control") { this },
                makeTestData("filtering") { filtering { it != 'b' } },
                makeTestData("sorting") { sorting { a, b -> b < a } },
                makeTestData("mapping") { mapping { it + 1 }.mapping { it - 1 } },
                makeTestData("sorting->filtering") { sorting { a, b -> b < a }.filtering { it != 'c' } },
                makeTestData("groupingBy->flatMapping") { groupingBy { it.toInt() / 2 }.flatMapping { it.second } },
                makeTestData("groupingBy/sorting->flatMapping") { groupingBy({ it.toInt() / 2 }, { it.sorting { a, b -> b < a } }).flatMapping { it.second } },
                makeTestData("multiGroupingBy->flatMapping") { multiGroupingBy({ setOf(it.toInt() / 2, it.toInt() / 3) }).flatMapping { it.second } },
                makeTestData("multiGroupingBy/sorting->flatMapping") { multiGroupingBy({ setOf(it.toInt() / 2, it.toInt() / 3) }, { it.sorting { a, b -> b < a } }).flatMapping { it.second } },
                makeTestData("The Gauntlet (easy)") {
                    filtering { it in 'g'..'i' }
                            .multiGroupingBy({ setOf(it.toInt() / 2, it.toInt() / 3) }, { it.sorting { a, b -> b < a } })
                            .flatMapping { it.second }
                },
                makeTestData("The Gauntlet") {
                    groupingBy { it.toInt() / 2 }
                            .flatMapping { it.second }
                            .filtering { it in 'g'..'i' }
                            .multiGroupingBy({ setOf(it.toInt() / 2, it.toInt() / 3) }, { it.sorting { a, b -> b < a } })
                            .flatMapping { it.second }
                            .filtering { it in 'i'..'j' }
                }
        )
    }

    @Test
    fun onAddTest() {
        makeTestDatas().forEachIndexed { index, it ->
            println("Test Data #$index: ${it.label}")
            repeat(3) { iteration ->
                val newElement = (newElement.toInt() + iteration).toChar()
//                println("Adding element $newElement")
                val callback = { item: Char, index: Int ->
                    assertEquals(newElement, item)
                    val validIndices = it.transformed.mapIndexedNotNull { index, it -> if (it == newElement) index else null }
                    assert(validIndices.contains(index), { "Index $index is not a valid index.  Valid indices are $validIndices" })
                }
                it.transformed.onAdd += callback
                when (iteration % 3) {
                    0 -> it.source.add(newElement)
                    1 -> it.source.add(0, newElement)
                    2 -> it.source.add(it.source.size / 2, newElement)
                    else -> it.source.add(newElement)
                }
                it.transformed.onAdd -= callback
            }
        }
    }

    @Test
    fun onAddFromStratchTest() {
        makeTestDatas().forEachIndexed { index, it ->
            println("Test Data #$index: ${it.label}")
            it.source.clear()
            val newElement = newElement
//                println("Adding element $newElement")
            val callback = { item: Char, index: Int ->
                assertEquals(newElement, item)
                val validIndices = it.transformed.mapIndexedNotNull { index, it -> if (it == newElement) index else null }
                assert(validIndices.contains(index), { "Index $index is not a valid index.  Valid indices are $validIndices" })
            }
            it.transformed.onAdd += callback
            it.source.add(newElement)
            it.transformed.onAdd -= callback
        }
    }

    @Test
    fun onRemoveTest() {
        makeTestDatas().forEachIndexed { index, it ->
            println("Test Data #$index: ${it.label}")
            repeat(it.source.size) { iteration ->
                val removingIndex = when (iteration % 3) {
                    0 -> it.source.size - 1
                    1 -> 0
                    2 -> it.source.size / 2
                    else -> it.source.size - 1
                }
                val removingElement = it.source[removingIndex]
                println("Removing element $removingElement")

                var validIndices: MutableList<Int> = ArrayList<Int>()

                val onChangeCallback = { old: Char, new: Char, index: Int ->
                    validIndices.add(index)
                    assertEquals(removingElement, old)
                    assertEquals(removingElement, new)
                }
                val onRemoveCallback = { item: Char, index: Int ->
                    assert(validIndices.contains(index), { "Index $index is not a valid index.  Valid indices are $validIndices" })
                    assertEquals(removingElement, item)
                    validIndices = validIndices.mapNotNull { if (it == index) null else if (it > index) it - 1 else it }.toMutableList()
                }

                it.transformed.onChange += onChangeCallback
                it.transformed.onRemove += onRemoveCallback
                it.source[removingIndex] = it.source[removingIndex]
                println(it.transformed.joinToString(transform = Char::toString))
                it.source.removeAt(removingIndex)
                it.transformed.onRemove -= onRemoveCallback
                it.transformed.onChange -= onChangeCallback
            }
        }
    }

    @Test
    fun onChangeTest() {
        makeTestDatas().forEachIndexed { index, it ->
            println("Test Data #$index: ${it.label}")
            for (i in it.source.indices) {
                val startElement = it.source[i]
                val newElement = (startElement.toInt() - 1).toChar()
                println("Modifying element $startElement to $newElement")
                val callback = { old: Char, new: Char, index: Int ->
                    assertEquals(startElement, old)
                    assertEquals(newElement, new)
                    val validIndices = it.transformed.mapIndexedNotNull { index, it -> if (it == newElement) index else null }
                    assert(validIndices.contains(index), { "Index $index is not a valid index.  Valid indices are $validIndices" })
                }
                it.transformed.onChange += callback
                it.source[i] = newElement
                it.transformed.onChange -= callback
            }
        }
    }

    @Test
    fun onUpdateAfterChangeTest() {
        makeTestDatas().forEachIndexed { index, it ->
            println("Test Data #$index: ${it.label}")
            for (i in it.source.indices) {
                val startElement = it.source[i]
                val newElement = (startElement.toInt() - 1).toChar()
//                println("Modifying element $startElement to $newElement")
                var uncalledOrBoth = true
                val callback = { old: Char, new: Char, index: Int ->
                    assertEquals(startElement, old)
                    assertEquals(newElement, new)
                    val validIndices = it.transformed.mapIndexedNotNull { index, it -> if (it == newElement) index else null }
                    assert(validIndices.contains(index), { "Index $index is not a valid index.  Valid indices are $validIndices" })
                    uncalledOrBoth = false
                }
                val onUpdateCallback = { list: List<Char> ->
                    uncalledOrBoth = true
                }
                it.transformed.onUpdate += onUpdateCallback
                it.transformed.onChange += callback
                it.source[i] = newElement
                it.transformed.onChange -= callback
                it.transformed.onUpdate -= onUpdateCallback
                assert(uncalledOrBoth)
            }
        }
    }
}