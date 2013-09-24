package controllers.s7_employment

import org.specs2.mutable.{Tags, Specification}
import play.api.test.WithBrowser
import controllers.{WithBrowserHelper, BrowserMatchers}

class G1BeenEmployedIntegrationSpec extends Specification with Tags {
  "Been Employed" should {
    "present, having indicated that the carer has been employed" in new WithBrowser with WithBrowserHelper with BrowserMatchers with EmployedSinceClaimDate {
    beginClaim()

      goTo("/employment/been-employed")
      back
      titleMustEqual("Your employment history - Employment History")
    }

    """be bypassed and go onto "other money" having indicated that "employment" is not required.""" in new WithBrowser with WithBrowserHelper with BrowserMatchers with NotEmployedSinceClaimDate {
      beginClaim()

      goTo("/employment/been-employed")
      titleMustEqual("Details about other money - About Other Money")
    }

    "start employment entry" in new WithBrowser with WithBrowserHelper with BrowserMatchers with EmployedSinceClaimDate {
      beginClaim()

      goTo("/employment/been-employed")
      back
      click("#beenEmployed_yes")
      next
      titleMustEqual("Your job - Employment History")
    }

    "show 1 error upon submitting no mandatory data" in new WithBrowser with WithBrowserHelper with BrowserMatchers with EmployedSinceClaimDate {
      beginClaim()

      goTo("/employment/been-employed")
      back
      next
      titleMustEqual("Your employment history - Employment History")
      findMustEqualSize("div[class=validation-summary] ol li", 1)
    }

    """continue to "completion" when there are no more "jobs" to submit.""" in new WithBrowser with WithBrowserHelper with BrowserMatchers with EmployedSinceClaimDate {
      beginClaim()

      goTo("/employment/been-employed")
      back
      click("#beenEmployed_no")
      next
      titleMustEqual("Completion - Employment History")
    }

    """go back to "education".""" in new WithBrowser with WithBrowserHelper with BrowserMatchers with EmployedSinceClaimDate {
      beginClaim()

      goTo("/education/completed")

      goTo("/employment/been-employed")
      back
      back
      titleMustEqual("Completion - About your education")
    }

    """remember "employment" upon stating "employment" and returning""" in new WithBrowser with WithBrowserHelper with BrowserMatchers with EmployedSinceClaimDate {
      beginClaim()

      goTo("/employment/been-employed")
      back
      titleMustEqual("Your employment history - Employment History")

      click("#beenEmployed_no")
      next
      titleMustEqual("Completion - Employment History")

      back
      titleMustEqual("Your employment history - Employment History")
      findFirst("#beenEmployed_yes").isSelected should beFalse
      findFirst("#beenEmployed_no").isSelected should beTrue
    }
  } section("integration", models.domain.Employed.id)
}