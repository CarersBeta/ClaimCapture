package controllers.circs.s3_consent_and_declaration

import org.specs2.mutable.{Tags, Specification}

class G1DeclarationFormSpec extends Specification with Tags {

  val infoAgreement = "yes"
  val why = "Cause i want"
  val confirm = "yes"

  "Change of circumstances - Declaration Form" should {
    "map data into case class" in {
      G1Declaration.form.bind(
        Map("obtainInfoAgreement" -> infoAgreement, "obtainInfoWhy" -> why, "confirm" -> confirm)
      ).fold(
        formWithErrors => "This mapping should not happen." must equalTo("Error"),
        f => {
          f.obtainInfoAgreement must equalTo(infoAgreement)
          f.obtainInfoWhy.get must equalTo(why)
          f.confirm must equalTo(confirm)
        }
      )
    }

    "reject special characters in text fields" in {
      G1Declaration.form.bind(
        Map("obtainInfoAgreement" -> infoAgreement, "obtainInfoWhy" -> "whyé", "confirm" -> confirm)
      ).fold(
        formWithErrors => {
          formWithErrors.errors.length must equalTo(1)
          formWithErrors.errors(0).message must equalTo("error.restricted.characters")
        },
        f => "This mapping should not happen." must equalTo("Valid"))
    }

  } section("unit", models.domain.CircumstancesConsentAndDeclaration.id)

}
