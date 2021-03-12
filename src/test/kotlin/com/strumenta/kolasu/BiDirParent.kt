package com.strumenta.kolasu.bidirparent

import com.strumenta.kolasu.ObservableList
import kotlin.reflect.KProperty

interface Node {
    var parent: Node?
    val children: List<Node>
    fun remove() {
        this.parent?.removeChild(this)
    }
    fun removeChild(child: Node)
}

abstract class AbstractNode : Node {
    override var parent: Node? = null
        set(value) {
            assert(this.parent.children.contains(this))
        }
    override val children : List<Node>
        get() = TODO()
    override fun removeChild(child: Node) {
        TODO("Not yet implemented")
    }
}

data class Author(val name: String) : AbstractNode() {


}

data class Book(val title: String) : AbstractNode() {

}

data class Publisher(val name: String) : AbstractNode() {
    val books by ManyContainment<Book>(this)
}

class ManyContainment<E:Node>(val container: Node) : ObservableList.ListObserver<E>{
    private val value = ObservableList<E>(this)

    operator fun getValue(container: Node, property: KProperty<*>): MutableList<E> {
        assert(container == this.container)
        return value
    }

    override fun added(element: E) {
        element.remove()
        element.parent = container
    }
}

fun main(args: Array<String>) {
    val p = Publisher("Feltrinelli")
    val b1 = Book("Red Book")
    p.books.add(b1)
}