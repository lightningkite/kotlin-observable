package com.lightningkite.kotlin.observable.list

import org.junit.Test
import java.util.*

/**
 * Created by joseph on 9/26/16.
 */
class StrictChainTest {
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
            val listening = ArrayList<Char>(it.source)

            val listenerSet = ObservableListListenerSet<Char>(
                    onAddListener = { item: Char, position: Int -> listening.add(position, item) },
                    onRemoveListener = { item: Char, position: Int -> listening.removeAt(position) },
                    onChangeListener = { old: Char, item: Char, position: Int -> listening[position] = item },
                    onMoveListener = { item: Char, oldPosition: Int, position: Int -> /*TODO*/ },
                    onReplaceListener = { list: ObservableList<Char> -> listening.clear(); listening.addAll(list) }
            )

            it.transformed.addListenerSet(listenerSet)

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

                val reference = it.transformer.invoke(it.source)

                assert(it.transformed deepEquals reference)
                assert(it.transformed.size.toString() == reference.size.toString())
                assert(listening deepEquals reference)
                assert(listening.size.toString() == reference.size.toString())


            }
            it.transformed.removeListenerSet(listenerSet)
            println("Dang, it passed")
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