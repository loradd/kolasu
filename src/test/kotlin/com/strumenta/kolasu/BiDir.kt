package com.strumenta.kolasu

import java.util.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

interface NNode

object CentralRelationshipManager {
    private val containersByDelegate = HashMap<ManyPropertyDelegate<*>, NNode>()
    private val propertiesByDelegate = HashMap<ManyPropertyDelegate<*>, KProperty<*>>()
    private val delegatesByProperty = HashMap<KProperty<*>, MutableList<ManyPropertyDelegate< *>>>()


    fun registerManyProperty(container: NNode, property: KProperty<*>, delegate: ManyPropertyDelegate< *>) {
        containersByDelegate[delegate] = container
        propertiesByDelegate[delegate] = property
        delegatesByProperty.getOrPut(property, { LinkedList() }).add(delegate)
    }

    fun <C:NNode> added(delegate: ManyPropertyDelegate<C>, element: C) {
        println("Added $element to ${containersByDelegate[delegate]}.${propertiesByDelegate[delegate]?.name}")
    }

    fun <P:NNode, C:NNode> getReverse(reverseThis: P, originalProperty: KProperty1<C, MutableList<P>>): ReverseList<C> {
        val delegates : List<ManyPropertyDelegate< *>> = delegatesByProperty[originalProperty] ?: emptyList()
        val initialElements : List<C> = delegates.mapNotNull {
            val container: NNode = containersByDelegate[it]!!
            val property: KProperty<*> = propertiesByDelegate[it]!!
            val contained = (it as ManyPropertyDelegate<C>).getValue(container as P, property) as List<C>
            val isInContained = contained.contains(reverseThis)
            val res: C? = if (isInContained) container as C else null
            res
        }.toList()
        return ReverseList<C>(initialElements)
    }
}

class ObservableList<E>(val observer: ListObserver<E>) : LinkedList<E>() {
    interface ListObserver<E> {
        fun added(element: E)
    }
    override fun add(element: E): Boolean {
        val res = super.add(element)
        if (res) {
            observer.added(element)
        }
        return res
    }
}

class ManyPropertyDelegate<C: NNode> : ReadOnlyProperty<NNode, MutableList<C>> {
    private var value : MutableList<C> = ObservableList(createObserver())
    private var registered = false

    init {
        //print("Creating MPD")
        value
    }

    private fun registerIfNeeded(thisRef: NNode, property: KProperty<*>) {
        if (!registered) {
            CentralRelationshipManager.registerManyProperty(thisRef, property, this)
            registered = true
        }
    }

//    override fun setValue(thisRef: P, property: KProperty<*>, value: MutableList<C>) {
//        registerIfNeeded(thisRef, property)
//        this.value = value
//    }

    override fun getValue(thisRef: NNode, property: KProperty<*>): MutableList<C> {
        registerIfNeeded(thisRef, property)
        return value
    }

    private fun createObserver() : ObservableList.ListObserver<C> {
        return object : ObservableList.ListObserver<C> {
            override fun added(element: C) {
                CentralRelationshipManager.added(this@ManyPropertyDelegate, element)
            }
        }
    }
}

class ReverseList<E:NNode>(initialElements: List<E>) : LinkedList<E>() {
    init {
        this.addAll(initialElements)
    }
    override fun add(element: E): Boolean {
        TODO()
    }
}

class Reverse<P: NNode, C: NNode>(val originalProperty: KProperty1<C, MutableList<P>>) : ReadOnlyProperty<P, MutableList<C>> {

    init {
    }

    override fun getValue(thisRef: P, property: KProperty<*>): MutableList<C> {
        return CentralRelationshipManager.getReverse(thisRef, originalProperty)
    }

}

data class Author(val name: String) : NNode {
    val written by ManyPropertyDelegate<Book>()

    fun print() {
        println("Books written by ${this.name}: ${this.written}")
    }
}

class Publisher(val name: String) : NNode {
    val publishedBooks: MutableList<Book> by ManyPropertyDelegate()

    override fun toString() = "Publisher($name)"
}

data class Book(val title: String) : NNode {
    val authors by Reverse(Author::written)

    fun print() {
        println("${this.title} authors: ${this.authors}")
    }
}

fun main(args: Array<String>) {
    val jp = Publisher("Johnny Publishing")
    val jp2 = Publisher("Johnny Publishing 2")
    val frank = Author("Frank")
    val bookA = Book("Book A")
    jp.publishedBooks.add(bookA)
    frank.written.add(bookA)

    frank.print()
    bookA.print()

    val bob = Author("Bob")
    val bookB = Book("Book B")
    val bookC = Book("Book C")
    bob.written.add(bookB)
    frank.written.add(bookC)
    bob.written.add(bookC)

    frank.print()
    bob.print()
    bookA.print()
    bookB.print()
    bookC.print()

//    val b2 = Book("Another book")
//    b2.authors.add(a)
//    println("Written books: ${a.written}")
//    println("Book authors: ${b2.authors}")

}