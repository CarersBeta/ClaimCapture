package utils.pageobjects.s1_carers_allowance

import play.api.test.WithBrowser
import utils.pageobjects._

final class G2EligibilityPage(ctx:PageObjectsContext) extends ClaimPage(ctx, G2EligibilityPage.url, G2EligibilityPage.title) {
  declareYesNo("#hours_answer", "CanYouGetCarersAllowanceDoYouSpend35HoursorMoreEachWeekCaring")
  declareYesNo("#over16_answer", "CanYouGetCarersAllowanceAreYouAged16OrOver")
  declareYesNo("#livesInGB_answer", "CanYouGetCarersAllowanceDoYouNormallyLiveinGb")
}

object G2EligibilityPage {
  val title = "Eligibility - Can you get Carer's Allowance?".toLowerCase

  val url = "/allowance/eligibility"

  def apply(ctx:PageObjectsContext) = new G2EligibilityPage(ctx)
}

trait G2EligibilityPageContext extends PageContext {
  this: WithBrowser[_] =>

  val page = G2EligibilityPage(PageObjectsContext(browser))
}