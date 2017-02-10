package helpers

import xml._

object XmlHelpers {
  def createAttribute(name: String, value: Option[Any]) = {
    value.map { result =>
      Attribute(None, name, Text(result.toString), Null)
    } getOrElse Null
  }

  def addChild(n: Node, newChild: Node): Node = n match {
    case Elem(prefix, label, attribs, scope, child @ _*) =>
      val nodes = child ++ newChild
      Elem.apply(prefix, label, attribs, scope, nodes.isEmpty, nodes : _*)
    case _ => sys.error("Can only add children to elements!")
  }

  def createElem(name: String): Elem = {
    val text = Text("")
    Elem.apply(null, name, Null, TopScope, text.isEmpty, text)
  }
}
