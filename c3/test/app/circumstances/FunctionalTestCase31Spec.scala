package app.circumstances

import play.api.test.{FakeApplication, WithBrowser}

import utils.pageobjects.{PageObjects, XmlPage, TestData, Page}
import utils.pageobjects.xml_validation.{XMLCircumstancesBusinessValidation, XMLBusinessValidation}
import app.FunctionalTestCommon
import utils.pageobjects.circumstances.s1_about_you.G1ReportAChangeInYourCircumstancesPage

class FunctionalTestCase31Spec extends FunctionalTestCommon {
  isolated

  "The application Circumstances" should {
    "Successfully run absolute Circumstances Test Case 31" in new WithBrowser(app = FakeApplication(additionalConfiguration = Map("circs.employment.active" -> "true"))) with PageObjects {
      pending("throws JavaScript error because test browser is not capable of handling the level of JS used")
      val page = G1ReportAChangeInYourCircumstancesPage(context)
      val circs = TestData.readTestDataFromFile("/functional_scenarios/circumstances/TestCase31.csv")
      page goToThePage()

      val lastPage = page runClaimWith(circs, XmlPage.title)

      lastPage match {
        case p: XmlPage => {
          val validator: XMLBusinessValidation = new XMLCircumstancesBusinessValidation
          validateAndPrintErrors(p, circs, validator) should beTrue
        }
        case p: Page => println(p.source())
      }
    }

  } section "functional"
}