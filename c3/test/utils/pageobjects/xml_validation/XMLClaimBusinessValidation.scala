package utils.pageobjects.xml_validation

import utils.pageobjects.{TestDatumValue, PageObjectException, TestData}
import scala.xml.{Node, Elem}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import scala.language.implicitConversions
import app.{StatutoryPaymentFrequency, PensionPaymentFrequency}

/**
 * Validates that an XML contains all the relevant data that was provided in a Claim.
 * The mapping claim's attributes to XML xPath is defined in a configuration file.
 * @author Jorge Migueis
 *         Date: 23/07/2013
 */
class XMLClaimBusinessValidation extends XMLBusinessValidation {
  val mappingFilename = "/ClaimScenarioXmlMapping.csv"

  def createXMLValidationNode = (xml: Elem, nodes: Array[String]) => new ClaimXmlNode(xml, nodes)

  /**
   * Performs the validation of a claim XML against the data used to populate the claim forms.
   * @param claim Original claim used to go through the screens and now used to validate XML.
   * @param xml  XML that needs to be validated against the provided claim.
   * @param throwException Specify whether the validation should throw an exception if mismatches are found.
   * @return List of errors found. The list is empty if no errors were found.
   */
  def validateXMLClaim(claim: TestData, xml: Elem, throwException: Boolean): List[String] = {
    super.validateXMLClaim(claim, xml, throwException, mappingFilename, createXMLValidationNode)
  }
}

/**
 * Represents an Xml Node once "cleaned", i.e. trimmed and line returns removed.
 */
class ClaimXmlNode(xml: Elem, path: Array[String]) extends XMLValidationNode(xml, path) {

  def matches(claimValue: TestDatumValue): Boolean = {
    import XMLValidationNode.prepareElement

    try {
      val nodeStart = theNodes(0).mkString

      val isARepeatableNode = !nodeStart.contains(EvidenceListNode) && !nodeStart.contains(DeclarationNode) && !nodeStart.contains("<Employed>") && !nodeStart.contains("<BreaksSinceClaim>")

      val isRepeatedAttribute = claimValue.attribute.contains( """_""")

      val iteration = if (isRepeatedAttribute) claimValue.attribute.split("_")(1).toInt - 1 else 0

      if (!isARepeatableNode && iteration > 0 && !nodeStart.contains(EvidenceListNode)) true
      else {
        val isPensionScheme = theNodes.mkString.contains("<PensionScheme>") // Because of bug in Schema :(  Do not like it

        val index = if (isRepeatedAttribute && isARepeatableNode && !isPensionScheme) iteration else 0

        val value = prepareElement(if (isPensionScheme) theNodes.text else theNodes(index).text)
        val nodeName = theNodes(index).mkString

        def valuesMatching: Boolean = {
          if (value.matches( """\d{4}-\d{2}-\d{2}[tT]\d{2}:\d{2}:\d{2}""") || nodeName.endsWith("OtherNames>") || nodeName.endsWith("PayerName>") || isPensionScheme) value.contains(claimValue.value)
          else if (nodeName.endsWith("Line>")) claimValue.value.contains(value)
          else if (nodeName.startsWith("<ClaimantActing")) nodeName.toLowerCase.contains(claimValue.value + ">" + value)
          else if (nodeName.startsWith(DeclarationNode)) claimValue.value == answerText(theNodes(index), "DeclarationQuestion",claimValue.question)
          else if (nodeName.startsWith(DisclaimerNode))  claimValue.value == answerText(theNodes(index), "DisclaimerQuestion", claimValue.question)
          else if (nodeName.startsWith(ConsentNode))  claimValue.value == answerText(theNodes(index), "Consent", claimValue.question)//TODO: If consent is no, check why
          else if (nodeName.endsWith("DateTime>")) value.contains(claimValue.value)
          else value == claimValue.value
        }

        val matching = valuesMatching

        if (!matching){
          error = " value expected: [" + (if (nodeName.startsWith(EvidenceListNode)) claimValue.question + "=" + claimValue.value else claimValue.value) + "] within value read: [" + value + "]"
        }

        matching
      }
    }
    catch {
      case e: IndexOutOfBoundsException => throw new PageObjectException("XML Validation failed" + this.toString() + " - " + claimValue.attribute)// + " Cause:"+e.getCause+" Trace:"+e.getStackTraceString)
    }
  }

  def answerText(node:Node,questionTag:String,questionLabel:String) = {
    XMLValidationNode.prepareElement(((node \\ questionTag).filter { n => XMLValidationNode.prepareElement(n \\ "QuestionLabel" text) == questionLabel } \\ "Answer").text)
  }

}

class ClaimValue(attribute: String, value: String, question: String) extends TestDatumValue(attribute, value, question) {}

object ClaimValue {

  private def prepareQuestion(question: String) = question.replace("\\n", "").replace("\n", "").replace(" ", "").trim.toLowerCase

  private def prepareClaimValue(claimValue: String, attribute: String) = {
    val cleanValue = claimValue.replace("\\n", "").replace(" ", "").trim.toLowerCase

    if (cleanValue.contains("/") && !attribute.startsWith("EmploymentLeavingDateP45") && !attribute.startsWith("AboutYouWhenDidYouArriveInYheUK")) {
      val date = DateTime.parse(cleanValue, DateTimeFormat.forPattern("dd/MM/yyyy"))
      date.toString(DateTimeFormat.forPattern("dd-MM-yyy"))
    } else cleanValue
  }

  def apply(attribute: String, value: String, question: String) = new ClaimValue(attribute, prepareClaimValue(value, attribute), prepareQuestion(question))
}