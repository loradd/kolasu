package com.strumenta.kolasu

import java.util.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

interface NNode

//class SingleRelationshipValue<T: NNode>
//class ManyRelationshipValue<T: NNode>
//
//class SinglePropertyDelegate<P: NNode, C: NNode> : ReadWriteProperty<P, C> {
//    override fun setValue(thisRef: P, property: KProperty<*>, value: C) {
//        TODO("Not yet implemented")
//    }
//
//    override fun getValue(thisRef: P, property: KProperty<*>): C {
//        TODO("Not yet implemented")
//    }
//}
//

//
//class ReverseList<E, P:NNode>(originalProperty: KProperty1<E, MutableList<P>>) : LinkedList<E>() {
//    override fun add(element: E): Boolean {
//        originalProperty.get(element).add(thisRef!!)
//        return super.add(element)
//    }
//}
//
//class Reverse<P: NNode, C: NNode>(originalProperty: KProperty1<C, MutableList<P>>) : ReadWriteProperty<P, MutableList<C>> {
//    private var thisRef : P? = null
//    private var value : MutableList<C> = object : LinkedList<C>() {
//        override fun add(element: C): Boolean {
//            originalProperty.get(element).add(thisRef!!)
//            return super.add(element)
//        }
//    }
//
//    init {
//        value
//    }
//
//    override fun setValue(thisRef: P, property: KProperty<*>, value: MutableList<C>) {
//        this.thisRef = thisRef
//        this.value = value
//    }
//
//    override fun getValue(thisRef: P, property: KProperty<*>): MutableList<C> {
//        this.thisRef = thisRef
//        return value
//    }
//
//
//}

//object Relation() {
//
//}

object CentralRelationshipManager {
    private val containersByDelegate = HashMap<ManyPropertyDelegate<*, *>, NNode>()
    private val propertiesByDelegate = HashMap<ManyPropertyDelegate<*, *>, KProperty<*>>()
    private val delegatesByProperty = HashMap<KProperty<*>, MutableList<ManyPropertyDelegate<*, *>>>()


    fun <P:NNode, C:NNode>registerManyProperty(container: P, property: KProperty<*>, delegate: ManyPropertyDelegate<P, C>) {
        containersByDelegate[delegate] = container
        propertiesByDelegate[delegate] = property
        delegatesByProperty.getOrPut(property, { LinkedList() }).add(delegate)
    }

    fun <P:NNode, C:NNode> added(delegate: ManyPropertyDelegate<P, C>, element: C) {
        println("Added $element to ${containersByDelegate[delegate]}.${propertiesByDelegate[delegate]?.name}")
    }

    fun <P:NNode, C:NNode> getReverse(reverseThis: P, originalProperty: KProperty1<C, MutableList<P>>): ReverseList<C> {
        val delegates = delegatesByProperty[originalProperty] ?: emptyList()
        delegates.map { it.getValue(containersByDelegate[it]!! as P, propertiesByDelegate[it]) }
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

class ManyPropertyDelegate<P: NNode, C: NNode> : ReadOnlyProperty<P, MutableList<C>> {
    private var value : MutableList<C> = ObservableList<C>(createObserver())
    private var registered = false

    init {
        //print("Creating MPD")
        value
    }

    private fun registerIfNeeded(thisRef: P, property: KProperty<*>) {
        if (!registered) {
            CentralRelationshipManager.registerManyProperty(thisRef, property, this)
            registered = true
        }
    }

//    override fun setValue(thisRef: P, property: KProperty<*>, value: MutableList<C>) {
//        registerIfNeeded(thisRef, property)
//        this.value = value
//    }

    override fun getValue(thisRef: P, property: KProperty<*>): MutableList<C> {
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

class ReverseList<E:NNode>() : LinkedList<E>() {
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
    val written: MutableList<Book> by ManyPropertyDelegate()
}

class Publisher(val name: String) : NNode {
    val publishedBooks: MutableList<Book> by ManyPropertyDelegate()

    override fun toString() = "Publisher($name)"
}

data class Book(val title: String) : NNode {
    val authors: MutableList<Author> by Reverse(Author::written)
}

fun main(args: Array<String>) {
    val jp = Publisher("Johnny Publishing")
    val jp2 = Publisher("Johnny Publishing 2")
    val frank = Author("Frank")
    val myBook = Book("My Book")
    jp.publishedBooks.add(myBook)
    frank.written.add(myBook)
    println("Books written by ${frank.name}: ${frank.written}")
    println("${myBook.title} authors: ${myBook.authors}")
//    val b2 = Book("Another book")
//    b2.authors.add(a)
//    println("Written books: ${a.written}")
//    println("Book authors: ${b2.authors}")

}