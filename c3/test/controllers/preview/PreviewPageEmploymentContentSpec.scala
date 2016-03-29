package controllers.preview

import org.specs2.mutable._
import utils.WithBrowser
import utils.pageobjects.{PageObjectsContext, PageObjects}
import utils.pageobjects.preview.PreviewPage
import utils.pageobjects.s_claim_date.GClaimDatePage
import controllers.ClaimScenarioFactory
import utils.pageobjects.s_employment.GEmploymentPage

class PreviewPageEmploymentContentSpec extends Specification {
  section("preview")
  "Preview Page" should {
    "display employment data" in new WithBrowser with PageObjects {
      fillEmploymentSection(context)
      val page =  PreviewPage(context)
      page goToThePage()
      val source = page.source

      source must contain("Employment and Self-Employment")
      source must contain("Have you been employed at any time since 10 April 2016?")
      source must contain("Yes")
      source must contain("Employment")
      source must contain("Tesco's")
      source must contain("From 01/01/2013 To 01/07/2013")
      source must contain("£600 every time including expenses")
      source must contain("Have you been self-employed at any time since 3 October 2016?")
      source must contain("Yes - Details provided including expenses")
    }
  }
  section("preview")

  def fillEmploymentSection (context:PageObjectsContext) = {
    val employmentData = ClaimScenarioFactory.s7Employment()
    val selfEmploymentData = ClaimScenarioFactory.s9SelfEmployment

    val claimDatePage = GClaimDatePage(context)
    claimDatePage goToThePage()
    val claimDate = ClaimScenarioFactory.s12ClaimDate()
    claimDatePage fillPageWith claimDate
    claimDatePage submitPage()

    val employmentPage = GEmploymentPage(context)
    employmentPage goToThePage ()
    employmentPage fillPageWith employmentData

    val selfEmploymentPage = employmentPage submitPage()
    selfEmploymentPage fillPageWith selfEmploymentData

    val selfEmployedAccountsPage = selfEmploymentPage submitPage()
    selfEmployedAccountsPage fillPageWith selfEmploymentData

    val selfEmployedPensionsPage = selfEmployedAccountsPage submitPage()
    selfEmployedPensionsPage fillPageWith selfEmploymentData

    val jobDetailsPage = selfEmployedPensionsPage submitPage()
    jobDetailsPage fillPageWith employmentData

    val lastWagePage = jobDetailsPage submitPage()
    lastWagePage fillPageWith employmentData

    val pensionsPage = lastWagePage submitPage()
    pensionsPage fillPageWith employmentData

    val beenEmployedPage = pensionsPage submitPage()
    employmentData.EmploymentHaveYouBeenEmployedAtAnyTime_1 = "No"
    beenEmployedPage fillPageWith employmentData

    beenEmployedPage submitPage()
  }
}
