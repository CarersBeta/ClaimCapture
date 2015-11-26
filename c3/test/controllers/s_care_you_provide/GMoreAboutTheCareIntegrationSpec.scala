package controllers.s_care_you_provide

import org.specs2.mutable._
import utils.WithBrowser
import controllers.{PreviewTestUtils, ClaimScenarioFactory, BrowserMatchers, Formulate}
import utils.pageobjects._
import utils.pageobjects.s_claim_date.GClaimDatePage
import utils.pageobjects.s_care_you_provide.{GTheirPersonalDetailsPage, GMoreAboutTheCarePage}
import utils.pageobjects.preview.PreviewPage
import utils.helpers.PreviewField._

class GMoreAboutTheCareIntegrationSpec extends Specification {
  sequential

  "Representatives For The Person" should {
    "be presented" in new WithBrowser with PageObjects {
      val page = GMoreAboutTheCarePage(context)
      page goToThePage()
    }

    "contain errors on invalid submission" in new WithBrowser with PageObjects {
      val page = GMoreAboutTheCarePage(context)
      page goToThePage()
      page submitPage()
      page.listErrors.size mustEqual 1
    }

    "navigate back" in new WithBrowser with PageObjects {
      val theirPersonalDetailsPage = GTheirPersonalDetailsPage(context)
      theirPersonalDetailsPage goToThePage()
      theirPersonalDetailsPage fillPageWith ClaimScenarioFactory.s4CareYouProvide(hours35 = true)
      val moreAboutTheCarePage = theirPersonalDetailsPage submitPage()
      moreAboutTheCarePage goBack() must beAnInstanceOf[GTheirPersonalDetailsPage]
    }

  }
  section ("integration", models.domain.CareYouProvide.id)

}
