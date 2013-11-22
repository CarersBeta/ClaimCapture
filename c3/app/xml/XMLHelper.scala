package xml

import scala.xml._
import scala.language.implicitConversions
import scala.reflect.ClassTag
import app.StatutoryPaymentFrequency
import app.XMLValues._
import models._
import models.PaymentFrequency
import models.MultiLineAddress
import models.PeriodFromTo
import models.NationalInsuranceNumber
import play.api.i18n.Messages

object XMLHelper {


  def stringify(value: Option[_], default: String = ""): String = value match {
    case Some(s: String) => formatValue(s)
    case Some(dmy: DayMonthYear) => dmy.`dd-MM-yyyy`
    case Some(nr: NationalInsuranceNumber) => nr.stringify
    case Some(sc: SortCode) => sc.stringify
    case Some(pf: PaymentFrequency) => pf.stringify
    case _ => default
  }

  // CJR : Note that I'm changing the text to format it
  def nodify(value: Option[_]): NodeSeq = value match {
    case Some(s: String) => Text(formatValue(s))
    case Some(dmy: DayMonthYear) => Text(dmy.`dd-MM-yyyy`)
    case Some(nr: NationalInsuranceNumber) => Text(nr.stringify)
    case Some(pf: PaymentFrequency) => paymentFrequency(pf)
    case Some(pft: PeriodFromTo) => fromToStructure(pft)
    case Some(sc: SortCode) => Text(sc.sort1 + sc.sort2 + sc.sort2)
    case _ => Text("")
  }

  def postalAddressStructure(addressOption: Option[MultiLineAddress], postcodeOption: Option[String]): NodeSeq = addressOption match {
    case Some(address:MultiLineAddress) => postalAddressStructure(address, postcodeOption.orNull)
    case _ => postalAddressStructure(new MultiLineAddress(), postcodeOption.orNull)
  }

  def postalAddressStructure(addressOption: MultiLineAddress, postcodeOption: Option[String]): NodeSeq = postalAddressStructure(addressOption, postcodeOption.getOrElse(""))

  def postalAddressStructure(address: MultiLineAddress, postcode: String): NodeSeq = {
    <Address>
      {if(address.lineOne.isEmpty){NodeSeq.Empty}else{<Line>{address.lineOne.orNull}</Line>}}
      {if(address.lineTwo.isEmpty){NodeSeq.Empty}else{<Line>{address.lineTwo.orNull}</Line>}}
      {if(address.lineThree.isEmpty){NodeSeq.Empty}else{<Line>{address.lineThree.orNull}</Line>}}
      {if(postcode == null || postcode == "") NodeSeq.Empty else <PostCode>{postcode.toUpperCase}</PostCode>}
    </Address>
  }

  def postalAddressStructureRecipientAddress(address: MultiLineAddress, postcode: String): NodeSeq = {
    <RecipientAddress>
      <Line>{address.lineOne.orNull}</Line>
      {if(address.lineTwo.isEmpty){NodeSeq.Empty}else{<Line>{address.lineTwo.orNull}</Line>}}
      {if(address.lineThree.isEmpty){NodeSeq.Empty}else{<Line>{address.lineThree.orNull}</Line>}}
      {if(postcode == null || postcode == "") NodeSeq.Empty else <PostCode>{postcode.toUpperCase}</PostCode>}
    </RecipientAddress>
  }

  def moneyStructure(amount: String) = {
    <Currency>{GBP}</Currency>
    <Amount>{amount}</Amount>
  }


  def question(questionLabelCode: String, answerText: String): NodeSeq = {
    val questionNode = <QuestionLabel>{Messages(questionLabelCode)}</QuestionLabel>
    questionNode ++ <Answer>{formatValue(answerText)}</Answer>
  }

  def questionCurrency(questionLabelCode: String, amount:String): NodeSeq = {
    val questionNode = <QuestionLabel>{Messages(questionLabelCode)}</QuestionLabel>
    questionNode ++ <Answer>{moneyStructure(amount)}</Answer>
  }

  def question(questionLabelCode: String, answer: Option[String]): NodeSeq = {
    val emptyAnswer = <Answer/>
    val questionNode = <QuestionLabel>{Messages(questionLabelCode)}</QuestionLabel>
    questionNode ++ optionalEmpty(answer,emptyAnswer)
  }

  def questionOther(questionLabelCode: String, answerText: String, otherText: Option[String]): NodeSeq = {
    val other = <Other/>
    val questionNode = <QuestionLabel>{Messages(questionLabelCode)}</QuestionLabel>
    questionNode ++ optionalEmpty(otherText,other) ++ <Answer>{formatValue(answerText)}</Answer>
  }

  // We should only see a why text supplied if the answer is no, but add the why text regardless if supplied
  def questionWhy(questionLabelCode: String, answerText: String, whyText: Option[String]): NodeSeq = {
    val why = <Why/>
    val questionNode = <QuestionLabel>{Messages(questionLabelCode)}</QuestionLabel>
    questionNode ++ <Answer>{formatValue(answerText)}</Answer> ++ optionalEmpty(whyText,why)
  }

  def questionWithMessageFormatted(questionLabelCode: String, answerText: String): NodeSeq = {
    val questionNode = <QuestionLabel>{questionLabelCode}</QuestionLabel>
    questionNode ++ <Answer>{formatValue(answerText)}</Answer>
  }

  def fromToStructure(period: Option[PeriodFromTo]): NodeSeq = {
    period.fold(
      (<DateFrom/><DateTo/>).asInstanceOf[NodeSeq]
    )(fromToStructure)
  }

  def fromToStructure(period: PeriodFromTo): NodeSeq = {
    <DateFrom>{period.from.`yyyy-MM-dd`}</DateFrom>
    <DateTo>{period.to.`yyyy-MM-dd`}</DateTo>
  }

  def paymentFrequency(freq: Option[PaymentFrequency]): NodeSeq = freq match {
    case Some(p) => paymentFrequency(p)
    case _ => NodeSeq.Empty
  }

  def paymentFrequency(freq: PaymentFrequency): NodeSeq =
    <PayFrequency>
      <QuestionLabel>job.pay.frequency</QuestionLabel>
      <Answer>{freq.frequency}</Answer>
      {
        freq.other match{
          case Some(s) => <Other>{s}</Other>
          case _ => NodeSeq.Empty
        }
      }
    </PayFrequency>

  def optional[T](option: Option[T], elem: Elem)(implicit classTag: ClassTag[T]): Elem = option match {
    case Some(o) => addChild(elem, nodify(option))
    case _ => elem
  }

  def optional[T](option: Option[T], elem: Elem, default: String)(implicit classTag: ClassTag[T]): Elem = option match {
    case Some(o) => addChild(elem, nodify(option))
    case _ => addChild(elem, Text(default))
  }

  def optionalEmpty[T](option: Option[T], elem: Elem)(implicit classTag: ClassTag[T]): NodeSeq = option match {
    case Some(o) => addChild(elem, nodify(option))
    case _ => NodeSeq.Empty
  }

  def addChild(n: Node, children: NodeSeq) = n match {
    case Elem(prefix, label, attribs, scope, child @ _*) => Elem(prefix, label, attribs, scope, true, child ++ children : _*)
    case _ => <error>failed adding children</error>
  }

  implicit def attachToNode(elem: Elem) = new {
    def +++[T](option: Option[T])(implicit classTag: ClassTag[T]): Elem = optional(option, elem)

    def +-[T](option: Option[T])(implicit classTag: ClassTag[T]): Elem = optional(option, elem, NotAsked)

    def +?[T](option: Option[T])(implicit classTag: ClassTag[T]): Elem = optional(option, elem, yes)

    def +!?[T](option: Option[T])(implicit classTag: ClassTag[T]): Elem = optional(option, elem, no)

    def ?+[T](option: Option[T])(implicit classTag: ClassTag[T]): NodeSeq = optionalEmpty(option, elem)
  }

  def booleanStringToYesNo(booleanString: String) = booleanString match {
    case "true" => Yes
    case "false" => No
    case null => ""
    case _ => booleanString
  }

  def titleCase(s: String) = if(s != null && s.length() > 0) s.head.toUpper + s.tail.toLowerCase else ""

  def formatValue(value:String):String = value match {
       case "yes" => Yes
       case "no" => No
       case "other" => Other
       case _ => value
     }

  def extractIdFrom(xml:Elem):String = {(xml \\ "TransactionId").text}

  // relies on the question function being passed
  def optionalQuestions (conditionField:String, parentNode:Node, questionNode:NodeSeq) = {
    conditionField.isEmpty match {
      case false => addChild(parentNode, questionNode)
      case true => NodeSeq.Empty
    }
  }

}