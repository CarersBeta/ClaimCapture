package controllers.s1_carers_allowance

import org.specs2.mutable.{Tags, Specification}
import play.api.test.WithBrowser
import utils.pageobjects.TestData
import utils.pageobjects.s2_about_you.G1YourDetailsPage
import utils.pageobjects.s1_carers_allowance.G6ApprovePageContext
import utils.pageobjects.s1_carers_allowance.G1BenefitsPageContext
import utils.pageobjects.s1_carers_allowance.G6ApprovePage
import utils.pageobjects.s1_carers_allowance.G4LivesInGBPage

class G5ApproveIntegrationSpec extends Specification with Tags {
  "Approve" should {
    "be presented" in new WithBrowser with G6ApprovePageContext {
      page goToThePage()
    }
  } section("integration",models.domain.CarersAllowance.id)

  "Carer's Allowance" should {
    val notRightPage: String = "Next Page is not of the right type."

    "be approved" in new WithBrowser with G1BenefitsPageContext {
      val claim = new TestData
      claim.CanYouGetCarersAllowanceDoesthePersonYouCareforGetOneofTheseBenefits = "Yes"
      claim.CanYouGetCarersAllowanceDoYouSpend35HoursorMoreEachWeekCaring = "Yes"
      claim.CanYouGetCarersAllowanceAreYouAged16OrOver = "Yes"
      claim.CanYouGetCarersAllowanceDoYouNormallyLiveinGb = "Yes"
      page goToThePage()
      page fillPageWith claim
      val hoursPage = page submitPage()
      hoursPage fillPageWith claim
      val over16Page = hoursPage submitPage()
      over16Page fillPageWith claim
      val livingGBPage = over16Page submitPage()
      livingGBPage fillPageWith claim
      val approvePage = livingGBPage submitPage()

      approvePage match {
        case p: G6ApprovePage => {
          p.ctx.previousPage must beSome(livingGBPage)
          p.isApproved must beTrue
        }
        case _ => ko(notRightPage)
      }
    }

    "be declined" in new WithBrowser with G1BenefitsPageContext {
      val claim = new TestData
      claim.CanYouGetCarersAllowanceDoesthePersonYouCareforGetOneofTheseBenefits = "Yes"
      claim.CanYouGetCarersAllowanceDoYouSpend35HoursorMoreEachWeekCaring = "Yes"
      claim.CanYouGetCarersAllowanceAreYouAged16OrOver = "Yes"
      claim.CanYouGetCarersAllowanceDoYouNormallyLiveinGb = "No"
      page goToThePage()
      val approvePage = page runClaimWith (claim, G6ApprovePage.title)

      approvePage match {
        case p: G6ApprovePage => {
          p.ctx.previousPage.get must beAnInstanceOf[G4LivesInGBPage]
          p.isNotApproved must beTrue
        }
        case _ => ko(notRightPage)
      }
    }

    "navigate to next section" in new WithBrowser with G1BenefitsPageContext {
      val claim = new TestData
      claim.CanYouGetCarersAllowanceDoesthePersonYouCareforGetOneofTheseBenefits = "Yes"
      claim.CanYouGetCarersAllowanceDoYouSpend35HoursorMoreEachWeekCaring = "Yes"
      claim.CanYouGetCarersAllowanceAreYouAged16OrOver = "Yes"
      claim.CanYouGetCarersAllowanceDoYouNormallyLiveinGb = "No"
      page goToThePage()
      page runClaimWith (claim, G1YourDetailsPage.title)
    }

    "contain the completed forms" in new WithBrowser with G1BenefitsPageContext {
      val claim = new TestData
      claim.CanYouGetCarersAllowanceDoesthePersonYouCareforGetOneofTheseBenefits = "no"
      claim.CanYouGetCarersAllowanceDoYouSpend35HoursorMoreEachWeekCaring = "yes"
      claim.CanYouGetCarersAllowanceAreYouAged16OrOver = "no"
      claim.CanYouGetCarersAllowanceDoYouNormallyLiveinGb = "yes"
      page goToThePage()
      page fillPageWith claim
      val s1g2 = page submitPage()
      
      s1g2 fillPageWith claim
      val s1g3 = s1g2 submitPage()
      
      s1g3 fillPageWith claim
      val s1g4 = s1g3 submitPage()
      
      s1g4 fillPageWith claim
      val s1g5 = s1g4 submitPage()

      s1g5 match {
        case p: G6ApprovePage => {
          p numberSectionsCompleted() mustEqual 4

          val completed = p.listCompletedForms

          completed(0) must contain("No")
          completed(1) must contain("Yes")
          completed(2) must contain("No")
          completed(3) must contain("Yes")
        }
        case _ => ko("Next Page is not of the right type.")
      }
    }
  } section("integration", models.domain.CarersAllowance.id)
}