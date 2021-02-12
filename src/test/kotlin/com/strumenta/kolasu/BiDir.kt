package com.strumenta.kolasu

import java.util.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

interface NNode

class SingleRelationshipValue<T: NNode>
class ManyRelationshipValue<T: NNode>

class SinglePropertyDelegate<P: NNode, C: NNode> : ReadWriteProperty<P, C> {
    override fun setValue(thisRef: P, property: KProperty<*>, value: C) {
        TODO("Not yet implemented")
    }

    override fun getValue(thisRef: P, property: KProperty<*>): C {
        TODO("Not yet implemented")
    }
}

class ManyPropertyDelegate<P: NNode, C: NNode> : ReadWriteProperty<P, MutableList<C>> {
    private var value : MutableList<C> = LinkedList<C>()

    init {
        value
    }

    override fun setValue(thisRef: P, property: KProperty<*>, value: MutableList<C>) {
        this.value = value
    }

    override fun getValue(thisRef: P, property: KProperty<*>): MutableList<C> {
        return value
    }
}

class ReverseList<E, P:NNode>(originalProperty: KProperty1<E, MutableList<P>>) : LinkedList<E>() {
    override fun add(element: E): Boolean {
        originalProperty.get(element).add(thisRef!!)
        return super.add(element)
    }
}

class Reverse<P: NNode, C: NNode>(originalProperty: KProperty1<C, MutableList<P>>) : ReadWriteProperty<P, MutableList<C>> {
    private var thisRef : P? = null
    private var value : MutableList<C> = object : LinkedList<C>() {
        override fun add(element: C): Boolean {
            originalProperty.get(element).add(thisRef!!)
            return super.add(element)
        }
    }

    init {
        value
    }

    override fun setValue(thisRef: P, property: KProperty<*>, value: MutableList<C>) {
        this.thisRef = thisRef
        this.value = value
    }

    override fun getValue(thisRef: P, property: KProperty<*>): MutableList<C> {
        this.thisRef = thisRef
        return value
    }


}

data class Author(val name: String) : NNode {
    val written: MutableList<Book> by Reverse(Book::authors)
}

class Publisher : NNode {
    val publishedBooks: List<Book> by ManyPropertyDelegate()
}

data class Book(val title: String) : NNode {
    val authors: MutableList<Author> by Reverse(Author::written)
}

fun main(args: Array<String>) {
    val p = Publisher()
    val a = Author("Frank")
    val b = Book("My Book")
    a.written.add(b)
    println("Written books: ${a.written}")
    println("Book authors: ${b.authors}")
    val b2 = Book("Another book")
    b2.authors.add(a)
    println("Written books: ${a.written}")
    println("Book authors: ${b2.authors}")

}