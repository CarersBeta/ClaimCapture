package xml

import models._

import scala.xml._
import scala.reflect.ClassTag
import scala.language.implicitConversions
import models.MultiLineAddress
import models.PeriodFromTo
import models.NationalInsuranceNumber
import scala.Some
import models.domain.AdditionalWageDetails
import scala.collection.mutable.ArrayBuffer
import models.PaymentFrequency
import models.MultiLineAddress
import models.PeriodFromTo
import models.NationalInsuranceNumber
import scala.Some

object XMLHelper {

  def stringify(value: Option[_]): String = {
    value match {
      case Some(s: String) => s
      case Some(dmy: DayMonthYear) => dmy.`yyyy-MM-dd`
      case Some(nr: NationalInsuranceNumber) => nr.toXmlString
      case _ => ""
    }
  }

  def nodify(value: Option[_]): NodeBuffer = {
    value  match {
      case Some(s: String) => new NodeBuffer += Text(s)
      case Some(dmy: DayMonthYear) => new NodeBuffer += Text(dmy.`yyyy-MM-dd`)
      case Some(nr: NationalInsuranceNumber) => new NodeBuffer += Text(nr.toXmlString)
      case Some(pf: PaymentFrequency) => paymentFrequency(pf)
      case Some(pft: PeriodFromTo) => fromToStructure(pft)
      case _ => new NodeBuffer += Text("")
    }
  }



  def postalAddressStructure(addressOption: Option[MultiLineAddress], postcodeOption: Option[String]): NodeBuffer = addressOption match {
    case Some(address:MultiLineAddress) => postalAddressStructure(address, postcodeOption.orNull)
    case _ =>  postalAddressStructure(MultiLineAddress(None, None, None), postcodeOption.orNull)
  }

  def postalAddressStructure(address: MultiLineAddress, postcode: String): NodeBuffer = {
    <gds:Line>{address.lineOne.orNull}</gds:Line>
    <gds:Line>{address.lineTwo.orNull}</gds:Line>
    <gds:Line>{address.lineThree.orNull}</gds:Line>
    <gds:PostCode>{postcode}</gds:PostCode>
  }

  def moneyStructure(amount:String) = {
    <Currency>GBP</Currency>
    <Amount>{amount}</Amount>
  }

  def fromToStructure(period: Option[PeriodFromTo]):NodeBuffer = {
    period.fold(
      <DateFrom/>
      <DateTo/>
    )(fromToStructure _)
  }

  def fromToStructure(period:PeriodFromTo):NodeBuffer = {
    <DateFrom>{period.from}</DateFrom>
    <DateTo>{period.to}</DateTo>
  }

  def paymentFrequency(freq:Option[PaymentFrequency]):NodeBuffer = {
    freq match {
      case Some(p) => paymentFrequency(p)
      case _ => new scala.xml.NodeBuffer() += <PayFrequency/> += <PayFrequencyOther/>
    }
  }


  def paymentFrequency(freq:PaymentFrequency):NodeBuffer = {
    new scala.xml.NodeBuffer() +=
    <PayFrequency>{freq.frequency}</PayFrequency> +=
    (freq.other match {
      case Some(s) => <PayFrequencyOther>s</PayFrequencyOther>
      case _ => <PayFrequencyOther/>
    })
  }

  def optional[T](option: Option[T],elem:Elem )(implicit classTag:ClassTag[T]): Elem = {
    option match {
      case Some(o) => addChild(elem,nodify(option))
      case _ => elem
    }
  }

  def optional[T](option: Option[T],elem:Elem,default:String)(implicit classTag:ClassTag[T]): Elem = {
    option match {
      case Some(o) => addChild(elem,nodify(option))
      case _ => addChild(elem,Text(default))
    }
  }

  def optionalEmpty[T](option: Option[T],elem:Elem)(implicit classTag:ClassTag[T]): NodeSeq = {
    option match {
      case Some(o) => addChild(elem,nodify(option))
      case _ => NodeSeq.Empty
    }
  }

  def addChild(n: Node, children: NodeSeq) = {
    n match {
      case Elem(prefix, label, attribs, scope, child @ _*) =>
        Elem(prefix, label, attribs, scope,true, child ++ children : _*)
      case _ => <error>failed adding children</error>
    }
  }

  implicit def attachToNode(elem: Elem) = new {
    def +++[T](option: Option[T])(implicit classTag:ClassTag[T]):Elem = {
      optional(option,elem)
    }

    def +?[T](option: Option[T])(implicit classTag:ClassTag[T]):Elem = {
      optional(option,elem,"yes")
    }
    def +!?[T](option: Option[T])(implicit classTag:ClassTag[T]):Elem = {
      optional(option,elem,"no")
    }

    def ?+[T](option: Option[T])(implicit classTag:ClassTag[T]):NodeSeq = {
      optionalEmpty(option,elem)
    }
  }
}