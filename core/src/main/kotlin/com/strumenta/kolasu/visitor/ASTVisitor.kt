package com.strumenta.kolasu.visitor

import com.strumenta.kolasu.model.Node

abstract class ASTVisitor {
    abstract fun dispatch(node: Node)
}