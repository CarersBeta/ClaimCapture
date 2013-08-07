package controllers.s1_carers_allowance

import org.specs2.mutable.{Tags, Specification}


class G4LivesInGBMandatoryFormSpec extends Specification with Tags {
  "Carer's Allowance - LivesInGB - Form" should {
    val answerYesNo = "yes"
      
    "map data into case class" in {
      G4LivesInGBMandatory.form.bind(
        Map("answer" -> answerYesNo)
      ).fold(
        formWithErrors => "This mapping should not happen." must equalTo("Error"),
        f => {
          f.answerYesNo must equalTo("yes")
        }
      )
    }

    "reject if mandatory field is not filled" in {
      G4LivesInGBMandatory.form.bind(
        Map("answer" -> "")
      ).fold(
        formWithErrors => formWithErrors.errors.head.message must equalTo("error.required"),
        f => "This mapping should not happen." must equalTo("Valid")
      )
    }
  } section "unit"

}
