package com.lightningkite.kotlin.observable.list

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
            val transformed: ObservableList<Char>,
            val transformer: List<Char>.() -> List<Char>
    )

    inline fun makeTestData(label: String, transforms: ObservableList<Char>.() -> ObservableList<Char>, noinline transformer: List<Char>.() -> List<Char>): TestData {
        val copy = observableListOf('b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p')
        return TestData(label, copy, copy.transforms(), transformer)
    }

    fun <T, G> List<T>.groupByMulti(grouper: (T) -> Collection<G>): Map<G, List<T>> {
        val destination = LinkedHashMap<G, ArrayList<T>>()
        for (it in this) {
            val keys = grouper(it)
            for (key in keys) {
                val list = destination.getOrPut(key) { ArrayList<T>() }
                list.add(it)
            }
        }
        return destination
    }

    fun makeTestDatas(): List<TestData> {
        return listOf(
                makeTestData("control", transforms = { this }, transformer = { this }),
                makeTestData("filtering", transforms = { filtering { it.toInt() % 2 == 0 } }, transformer = { filter { it.toInt() % 2 == 0 } }),
                makeTestData("sorting", transforms = { sorting { a, b -> b < a } }, transformer = { this.sortedDescending() }),
                makeTestData("mapping", transforms = { mapping { it + 2 }.mapping { it - 1 } }, transformer = { map { it + 2 }.map { it - 1 } }),
                makeTestData(
                        "sorting->filtering",
                        transforms = { sorting { a, b -> b < a }.filtering { it.toInt() % 2 == 0 } },
                        transformer = { sortedDescending().filter { it.toInt() % 2 == 0 } }
                ),
                makeTestData(
                        "groupingBy->flatMapping",
                        transforms = { groupingBy { it.toInt() / 2 }.flatMapping { it.second } },
                        transformer = { groupBy { it.toInt() / 2 }.flatMap { it.value } }
                ),
                makeTestData(
                        "groupingBy/sorting->flatMapping",
                        transforms = { groupingBy({ it.toInt() / 2 }, { it.sorting { a, b -> b < a } }).flatMapping { it.second } },
                        transformer = { groupBy { it.toInt() / 2 }.flatMap { it.value.sortedDescending() } }
                ),
                makeTestData(
                        "multiGroupingBy->flatMapping",
                        transforms = { multiGroupingBy({ setOf(it.toInt() / 2, it.toInt() / 3) }).flatMapping { it.second } },
                        transformer = { groupByMulti { setOf(it.toInt() / 2, it.toInt() / 3) }.flatMap { it.value } }
                ),
                makeTestData(
                        "multiGroupingBy/sorting->flatMapping",
                        transforms = { multiGroupingBy({ setOf(it.toInt() / 2, it.toInt() / 3) }, { it.sorting { a, b -> b < a } }).flatMapping { it.second } },
                        transformer = { groupByMulti { setOf(it.toInt() / 2, it.toInt() / 3) }.flatMap { it.value.sortedDescending() } }
                ),
                makeTestData(
                        "The Gauntlet (easy)",
                        transforms = {
                            filtering { it in 'g'..'i' }
                                    .multiGroupingBy({ setOf(it.toInt() / 2, it.toInt() / 3) }, { it.sorting { a, b -> b < a } })
                                    .flatMapping { it.second }
                        },
                        transformer = {
                            filter { it in 'g'..'i' }.groupByMulti { setOf(it.toInt() / 2, it.toInt() / 3) }.flatMap { it.value.sortedDescending() }
                        }
                ),
                makeTestData(
                        "The Gauntlet",
                        transforms = {
                            groupingBy { it.toInt() / 2 }
                                    .flatMapping { it.second }
                                    .filtering { it in 'c'..'i' }
                                    .multiGroupingBy({ setOf(it.toInt() / 2, it.toInt() / 3) }, { it.sorting { a, b -> b < a } })
                                    .flatMapping { it.second }
                                    .filtering { it in 'e'..'j' }
                        },
                        transformer = {
                            groupBy { it.toInt() / 2 }
                                    .flatMap { it.value }
                                    .filter { it in 'c'..'i' }
                                    .groupByMulti { setOf(it.toInt() / 2, it.toInt() / 3) }
                                    .flatMap { it.value.sortedDescending() }
                                    .filter { it in 'e'..'j' }
                        }
                )
        )
    }
//
//    @Test
//    fun onAddTest() {
//        makeTestDatas().forEachIndexed { index, it ->
//            println("Test Data #$index: ${it.label}")
//            repeat(3) { iteration ->
//                val newElement = (newElement.toInt() + iteration).toChar()
////                println("Adding element $newElement")
//                val callback = { item: Char, index: Int ->
//                    assertEquals(newElement, item)
//                    val validIndices = it.transformed.mapIndexedNotNull { index, it -> if (it == newElement) index else null }
//                    assert(validIndices.contains(index), { "Index $index is not a valid index.  Valid indices are $validIndices" })
//                }
//                it.transformed.onAdd += callback
//                when (iteration % 3) {
//                    0 -> it.source.add(newElement)
//                    1 -> it.source.add(0, newElement)
//                    2 -> it.source.add(it.source.size / 2, newElement)
//                    else -> it.source.add(newElement)
//                }
//                it.transformed.onAdd -= callback
//            }
//        }
//    }
//
//    @Test
//    fun onAddFromStratchTest() {
//        makeTestDatas().forEachIndexed { index, it ->
//            println("Test Data #$index: ${it.label}")
//            it.source.clear()
//            val newElement = newElement
////                println("Adding element $newElement")
//            val callback = { item: Char, index: Int ->
//                assertEquals(newElement, item)
//                val validIndices = it.transformed.mapIndexedNotNull { index, it -> if (it == newElement) index else null }
//                assert(validIndices.contains(index), { "Index $index is not a valid index.  Valid indices are $validIndices" })
//            }
//            it.transformed.onAdd += callback
//            it.source.add(newElement)
//            it.transformed.onAdd -= callback
//        }
//    }
//
//    @Test
//    fun onRemoveTest() {
//        makeTestDatas().forEachIndexed { index, it ->
//            println("Test Data #$index: ${it.label}")
//            repeat(it.source.size) { iteration ->
//                val removingIndex = when (iteration % 3) {
//                    0 -> it.source.size - 1
//                    1 -> 0
//                    2 -> it.source.size / 2
//                    else -> it.source.size - 1
//                }
//                val removingElement = it.source[removingIndex]
//                println("Removing element $removingElement")
//
//                var validIndices: MutableList<Int> = ArrayList<Int>()
//
//                val onChangeCallback = { old: Char, new: Char, index: Int ->
//                    validIndices.add(index)
//                    assertEquals(removingElement, old)
//                    assertEquals(removingElement, new)
//                }
//                val onRemoveCallback = { item: Char, index: Int ->
//                    assert(validIndices.contains(index), { "Index $index is not a valid index.  Valid indices are $validIndices" })
//                    assertEquals(removingElement, item)
//                    validIndices = validIndices.mapNotNull { if (it == index) null else if (it > index) it - 1 else it }.toMutableList()
//                }
//
//                it.transformed.onChange += onChangeCallback
//                it.transformed.onRemove += onRemoveCallback
//                it.source[removingIndex] = it.source[removingIndex]
//                println(it.transformed.joinToString(transform = Char::toString))
//                it.source.removeAt(removingIndex)
//                it.transformed.onRemove -= onRemoveCallback
//                it.transformed.onChange -= onChangeCallback
//            }
//        }
//    }
//
//    @Test
//    fun onChangeTest() {
//        makeTestDatas().forEachIndexed { index, it ->
//            println("Test Data #$index: ${it.label}")
//            for (i in it.source.indices) {
//                val startElement = it.source[i]
//                val newElement = (startElement.toInt() - 1).toChar()
//                println("Modifying element $startElement to $newElement")
//                val callback = { old: Char, new: Char, index: Int ->
//                    assertEquals(startElement, old)
//                    assertEquals(newElement, new)
//                    val validIndices = it.transformed.mapIndexedNotNull { index, it -> if (it == newElement) index else null }
//                    assert(validIndices.contains(index), { "Index $index is not a valid index.  Valid indices are $validIndices" })
//                }
//                it.transformed.onChange += callback
//                it.source[i] = newElement
//                it.transformed.onChange -= callback
//            }
//        }
//    }
//
//    @Test
//    fun onUpdateAfterChangeTest() {
//        makeTestDatas().forEachIndexed { index, it ->
//            println("Test Data #$index: ${it.label}")
//            for (i in it.source.indices) {
//                val startElement = it.source[i]
//                val newElement = (startElement.toInt() - 1).toChar()
////                println("Modifying element $startElement to $newElement")
//                var uncalledOrBoth = true
//                val callback = { old: Char, new: Char, index: Int ->
//                    assertEquals(startElement, old)
//                    assertEquals(newElement, new)
//                    val validIndices = it.transformed.mapIndexedNotNull { index, it -> if (it == newElement) index else null }
//                    assert(validIndices.contains(index), { "Index $index is not a valid index.  Valid indices are $validIndices" })
//                    uncalledOrBoth = false
//                }
//                val onUpdateCallback = { list: List<Char> ->
//                    uncalledOrBoth = true
//                }
//                it.transformed.onUpdate += onUpdateCallback
//                it.transformed.onChange += callback
//                it.source[i] = newElement
//                it.transformed.onChange -= callback
//                it.transformed.onUpdate -= onUpdateCallback
//                assert(uncalledOrBoth)
//            }
//        }
//    }
//
//    @Test
//    fun removeAllTest() {
//        makeTestDatas().forEachIndexed { index, it ->
//            println("Test Data #$index: ${it.label}")
//            it.source.removeAll(listOf('b', 'c', 'd', 'p'))
//            println(it.transformed.joinToString(transform = Char::toString))
//        }
//    }

    @Test
    fun theRandomScrambler() {
        val seed = 8682522807148012L xor System.nanoTime()
        val random = Random(seed)
        println("Seed = $seed")
        makeTestDatas().forEachIndexed { index, it ->
            println("Test Data #$index: ${it.label}")
            repeat(10000) { index ->
                val op = random.nextInt(5)
                if (it.source.size < 5)
                    it.source.add(0, random.nextLowercaseLetter())
                else if (it.source.size > 26)
                    it.source.removeAll { it.toInt() % 3 == 0 }
                else when (op) {
                    0 -> it.source.add(random.nextInt(it.source.size), random.nextLowercaseLetter())
                    1 -> it.source.removeAt(random.nextInt(it.source.size))
                    2 -> it.source.set(random.nextInt(it.source.size), random.nextLowercaseLetter())
                    3 -> it.source.removeAll { it == random.nextLowercaseLetter() }
                    4 -> it.source.addAll(listOf(random.nextLowercaseLetter(), random.nextLowercaseLetter()))
                }
//                println("Last op $op")
                it.transformed.asSequence().count()
//                println(it.source.joinToString(transform = Char::toString))

            }
            println(it.transformed.toSet().sorted() deepEquals it.transformer.invoke(it.source).toSet().sorted())
            println(it.transformed.size.toString() + " VS " + it.transformer.invoke(it.source).size.toString())
            assert(it.transformed.toSet().sorted() deepEquals it.transformer.invoke(it.source).toSet().sorted())
            assert(it.transformed.size.toString() == it.transformer.invoke(it.source).size.toString())

            println(it.transformed.joinToString(transform = Char::toString))
            println(it.transformer.invoke(it.source).joinToString(transform = Char::toString))
        }
    }

    infix fun <T> List<T>.deepEquals(other: List<T>): Boolean {
        if (size != other.size) return false
        for (index in indices) {
            if (this[index] != other[index]) return false
        }
        return true
    }

    fun Random.nextLowercaseLetter(): Char = nextInt('z' - 'a').plus('a'.toInt()).toChar()
}