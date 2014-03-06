package utils.pageobjects.S11_consent_and_declaration

import play.api.test.WithBrowser
import utils.pageobjects._

/**
 * Page Object for S10 G1 Additional Information.
 * @author Jorge Migueis
 *         Date: 02/08/2013
 */
class G1AdditionalInfoPage (ctx:PageObjectsContext) extends ClaimPage(ctx, G1AdditionalInfoPage.url, G1AdditionalInfoPage.title) {
  declareYesNo("#welshCommunication", "ConsentDeclarationCommunicationWelsh")
  declareYesNo("#anythingElse_answer", "ConsentDeclarationTellUsAnythingElseAnswerAboutClaim")
  declareInput("#anythingElse_text", "ConsentDeclarationTellUsAnythingElseTextAboutClaim")
}

/**
 * Companion object that integrates factory method.
 * It is used by PageFactory object defined in Page.scala
 */
object G1AdditionalInfoPage {
  val title = "Additional information - Consent and Declaration".toLowerCase

  val url = "/consent-and-declaration/additional-info"

  def apply(ctx:PageObjectsContext,previousPage: Option[Page] = None) = new G1AdditionalInfoPage(ctx)
}

/** The context for Specs tests */
trait G1AdditionalInfoPageContext extends PageContext {
  this: WithBrowser[_] =>

  val page = G1AdditionalInfoPage (PageObjectsContext(browser))
}