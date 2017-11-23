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
                        "groupingBy/sorting->sorting->flatMapping",
                        transforms = { groupingBy({ it.toInt() / 4 }, { it.sorting { a, b -> b < a } }).sorting { a, b -> a.first < b.first }.flatMapping { it.second } },
                        transformer = { groupBy { it.toInt() / 4 }.entries.sortedBy { it.key }.flatMap { it.value.sortedDescending() } }
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
                assert(it.transformed.toSet().sorted() deepEquals it.transformer.invoke(it.source).toSet().sorted())
                assert(it.transformed.size.toString() == it.transformer.invoke(it.source).size.toString())
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