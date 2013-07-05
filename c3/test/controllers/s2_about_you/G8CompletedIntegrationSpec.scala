package controllers.s2_about_you

import org.specs2.mutable.{Tags, Specification}
import play.api.test.WithBrowser
import org.specs2.execute.PendingUntilFixed
import controllers.FormHelper

class G8CompletedIntegrationSpec extends Specification with Tags {

  "About You" should {
    "be presented" in new WithBrowser {
      browser.goTo("/aboutyou/completed")
      browser.title mustEqual "Completion - About You"
    }

    """be submitted to "Personal Details - Your Partner" page when they have had a partner/spouse at any time since the claim date""" in new WithBrowser {
      FormHelper.fillYourDetails(browser)
      FormHelper.fillYourContactDetails(browser)
      FormHelper.fillClaimDate(browser)
      FormHelper.fillMoreAboutYou(browser)
      FormHelper.fillEmployment(browser)
      FormHelper.fillPropertyAndRent(browser)
      
      browser.goTo("/aboutyou/completed")
      browser.submit("button[type='submit']")
      browser.title mustEqual "Personal Details - Your Partner"  
    }

    """be submitted to "Their Personal Details - Care You Provide" page when they have NOT had a partner/spouse at any time since the claim date""" in new WithBrowser {
      FormHelper.fillYourDetails(browser)
      FormHelper.fillYourContactDetails(browser)
      FormHelper.fillClaimDate(browser)
      FormHelper.fillMoreAboutYouNotHadPartnerSinceClaimDate(browser)
      FormHelper.fillEmployment(browser)
      FormHelper.fillPropertyAndRent(browser)
      
      browser.goTo("/aboutyou/completed")
      browser.submit("button[type='submit']")
      browser.title mustEqual "Their Personal Details - Care You Provide"
    }
    
  } section "integration"
}