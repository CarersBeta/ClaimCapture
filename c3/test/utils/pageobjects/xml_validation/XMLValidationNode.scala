package utils.pageobjects.xml_validation

import scala.xml.{Elem, NodeSeq}

/**
 * Represents an XML Node once "cleaned", i.e. trimmed and line returns removed.
 */
abstract class XMLValidationNode(xml: Elem, path:Array[String]) {

  val theNodes = XMLValidationNode.childNode(xml.\\(path(0)), path.drop(1))

  var error = ""

  def matches(claimValue: ClaimValue): Boolean

  def doesNotMatch(claimValue: ClaimValue): Boolean = !matches(claimValue)

  def size = theNodes.size

  def isDefined = theNodes.nonEmpty

  override def toString = theNodes.mkString(",")
}

object XMLValidationNode {

  def prepareElement(elementValue: String) = elementValue.replace("\\n", "").replace("\n", "").replace(" ", "").trim.toLowerCase

  // Internal function used to recursively go through the xPath provided to find value
  def childNode(xml: NodeSeq, children: Array[String]): NodeSeq =
    if (children.size == 0) xml else childNode(xml \ children(0), children.drop(1))

}