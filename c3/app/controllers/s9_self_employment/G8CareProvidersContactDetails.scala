package controllers.s9_self_employment

import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.Controller
import controllers.Mappings._
import models.domain.{Claim, CareProvidersContactDetails}
import models.view.CachedClaim
import utils.helpers.CarersForm._



object G8CareProvidersContactDetails extends Controller with CachedClaim {
  def completedQuestionGroups(implicit claim: Claim) = claim.completedQuestionGroups(CareProvidersContactDetails)
  val formCall = routes.G8CareProvidersContactDetails.present()

  val form = Form(
    mapping(
      call(formCall),
      "address" -> optional(address),
      "postcode" -> optional(text verifying validPostcode)
    )(CareProvidersContactDetails.apply)(CareProvidersContactDetails.unapply)
  )

  def present = claiming { implicit claim => implicit request =>
    //Ok(views.html.s9_self_employment.g1_aboutSelfEmployment(form, completedQuestionGroups))
    Ok(<p>Hello, world!</p>)
  }

  def submit = claiming { implicit claim =>
    implicit request =>
      /*form.bindEncrypted.fold(
        formWithErrors => BadRequest(views.html.s9_self_employment.g1_aboutSelfEmployment(formWithErrors, completedQuestionGroups)),
        f => claim.update(f) -> Redirect(routes.G1AboutSelfEmployment.present())
      )*/
      Ok(<p>Hello, world!</p>)
  }
}
