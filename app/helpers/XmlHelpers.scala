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
      Elem(prefix, label, attribs, scope, child ++ newChild : _*)
    case _ => error("Can only add children to elements!")
  }
}
