package controllers.s11_consent_and_declaration

import play.api.mvc._
import models.view.CachedClaim
import models.view.Navigable
import controllers.submission._
import monitoring.ClaimBotChecking
import play.api.data.Form
import play.api.data.Forms._
import models.domain.JSEnabled
import services.submission.ClaimSubmissionService
import services.ClaimTransactionComponent

class G5Submit extends Controller with CachedClaim with Navigable
      with AsyncSubmissionController
      with ClaimBotChecking
      with ClaimSubmissionService
      with ClaimTransactionComponent {
  val claimTransaction = new ClaimTransaction

  val form = Form(mapping(
    "jsEnabled" -> boolean
  )(JSEnabled.apply)(JSEnabled.unapply))

  def present = claimingWithCheck {
    implicit claim => implicit request => implicit lang =>
      track(models.domain.Submit) {
        implicit claim => Ok(views.html.s11_consent_and_declaration.g5_submit())
      }
  }

  def submit: Action[AnyContent] = claiming {
    implicit claim => implicit request => implicit lang =>
      form.bindFromRequest.fold(
        formWithErrors => {
          NotFound
        },
        f => {
          println(claim.getClass) // class models.view.CachedClaim$$anon$2
          checkForBot(claim, request)
          submission(claim, request, f.jsEnabled)
        }
      )
  }
}





