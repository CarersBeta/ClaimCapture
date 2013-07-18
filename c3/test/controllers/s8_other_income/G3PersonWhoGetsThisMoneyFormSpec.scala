package controllers.s8_other_money

import org.specs2.mutable.{Tags, Specification}
import models.DayMonthYear
import scala.Some
import models.NationalInsuranceNumber

class G3PersonWhoGetsThisMoneyFormSpec extends Specification with Tags {
  "Person Who Gets This Money Form" should {
    val fullName = "Donald Duck"
    val ni1 = "AB"
    val ni2 = 12
    val ni3 = 34
    val ni4 = 56
    val ni5 = "C"
    val nameOfBenefit = "foo"
    
    "map data into case class" in {
      G3PersonWhoGetsThisMoney.form.bind(
        Map(
          "fullName" -> fullName,
          "nationalInsuranceNumber.ni1" -> ni1,
          "nationalInsuranceNumber.ni2" -> ni2.toString,
          "nationalInsuranceNumber.ni3" -> ni3.toString,
          "nationalInsuranceNumber.ni4" -> ni4.toString,
          "nationalInsuranceNumber.ni5" -> ni5,
          "nameOfBenefit" -> nameOfBenefit
        )
      ).fold(
        formWithErrors => "This mapping should not happen." must equalTo("Error"),
        f => {
          f.fullName must equalTo(fullName)
          f.nationalInsuranceNumber must equalTo(Some(NationalInsuranceNumber(Some(ni1), Some(ni2.toString), Some(ni3.toString), Some(ni4.toString), Some(ni5))))
          f.nameOfBenefit must equalTo(nameOfBenefit)
        }
      )
    }
  }
}