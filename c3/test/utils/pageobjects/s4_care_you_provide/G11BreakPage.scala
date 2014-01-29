package utils.pageobjects.s4_care_you_provide

import play.api.test.WithBrowser
import utils.pageobjects._

final class G11BreakPage(ctx:PageObjectsContext, iteration: Int) extends ClaimPage(ctx, G11BreakPage.url, G11BreakPage.title, iteration) {
  declareDate("#start", "AboutTheCareYouProvideBreakStartDate_" + iteration)
  declareDate("#end", "AboutTheCareYouProvideBreakEndDate_" + iteration)
  declareTime("#start", "AboutTheCareYouProvideBreakStartTime_" + iteration)
  declareTime("#end", "AboutTheCareYouProvideBreakEndTime_" + iteration)
  declareSelect("#whereYou_location", "AboutTheCareYouProvideWhereWereYouDuringTheBreak_" + iteration)
  declareInput("#whereYou_location_other", "AboutTheCareYouProvideWhereWereYouDuringTheBreakOther_" + iteration)
  declareSelect("#wherePerson_location", "AboutTheCareYouProvideWhereWasThePersonYouCareForDuringtheBreak_" + iteration)
  declareInput("#wherePerson_location_other", "AboutTheCareYouProvideWhereWasThePersonYouCareForDuringtheBreakOther_" + iteration)
  declareYesNo("#medicalDuringBreak", "AboutTheCareYouProvideDidYouOrthePersonYouCareForGetAnyMedicalTreatment_" + iteration)

  /**
   * Called by submitPage of Page. A new G10 will be built with an incremented iteration number.
   * @return Incremented iteration number.
   */
  protected override def getNewIterationNumber = {
    import IterationManager._
    ctx.iterationManager.increment(Breaks)
  }
}

/**
 * Companion object that integrates factory method.
 * It is used by PageFactory object defined in PageFactory.scala
 */
object G11BreakPage {
  val title = "Break - About the care you provide".toLowerCase

  val url  = "/care-you-provide/break"

  def apply(ctx:PageObjectsContext, iteration:Int) = new G11BreakPage(ctx,iteration)
}

/** The context for Specs tests */
trait G11BreakPageContext extends PageContext {
  this: WithBrowser[_] =>

  val page = G11BreakPage (PageObjectsContext(browser), iteration = 1)
}