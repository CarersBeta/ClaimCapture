package utils.pageobjects.s3_your_partner

import play.api.test.{WithBrowser, TestBrowser}
import utils.pageobjects.{PageContext, Page}

/**
 * Page Object for s3_yourPartnerDetails g2_yourPartnerContactDetails
 * @author Saqib Kayani
 *         Date: 22/07/2013
 */
final class G2YourPartnerContactDetailsPage (browser: TestBrowser, previousPage: Option[Page] = None) extends Page(browser, G2YourPartnerContactDetailsPage.url, G2YourPartnerContactDetailsPage.title, previousPage){
  declareAddress("#address", "AboutYourPartnerAddress")
  declareInput("#postcode", "AboutYourPartnerPostcode")
}

/**
 * Companion object that integrates factory method.
 * It is used by PageFactory object defined in PageFactory.scala
 */
object G2YourPartnerContactDetailsPage {
  val title = "Contact Details - About your partner/spouse"

  val url  = "/your-partner/contact-details"

  def buildPageWith(browser: TestBrowser, previousPage: Option[Page] = None) = new G2YourPartnerContactDetailsPage(browser,previousPage)
}

/** The context for Specs tests */
trait G2YourPartnerContactDetailsPageContext extends PageContext {
  this: WithBrowser[_] =>

  val page = G2YourPartnerContactDetailsPage buildPageWith browser
}