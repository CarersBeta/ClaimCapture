package controllers.s_disclaimer

import org.specs2.mutable._
import utils.WithBrowser
import utils.pageobjects.s_eligibility.GApprovePage
import utils.pageobjects.PageObjects
import utils.pageobjects.s_disclaimer.GDisclaimerPage
import utils.pageobjects.third_party.GThirdPartyPage

class GDisclaimerIntegrationSpec extends Specification {
  section("integration", models.domain.DisclaimerSection.id)
  "Disclaimer" should {
    "be presented" in new WithBrowser with PageObjects {
      val page = GDisclaimerPage(context)
      page goToThePage()
    }

    "navigate to next page on valid submission" in new WithBrowser with PageObjects {
      val disclaimerPage = GDisclaimerPage(context) goToThePage()
      val thirdPartyPage = disclaimerPage submitPage()

      thirdPartyPage must beAnInstanceOf[GThirdPartyPage]
    }

    "navigate back to approve page" in new WithBrowser with PageObjects {
      val approvePage =  GApprovePage(context) goToThePage()
      val disclaimerPage = approvePage submitPage()

      disclaimerPage goBack() must beAnInstanceOf[GApprovePage]
    }
  }
  section("integration", models.domain.DisclaimerSection.id)
}
