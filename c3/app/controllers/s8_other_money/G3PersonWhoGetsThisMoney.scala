package controllers.s8_other_money

import controllers.Mappings._
import models.domain.Claim
import models.domain.PersonWhoGetsThisMoney
import models.view.CachedClaim
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.Controller
import utils.helpers.CarersForm.formBinding

object G3PersonWhoGetsThisMoney extends Controller with CachedClaim {
  val form = Form(
    mapping(
      "fullName" -> nonEmptyText(maxLength = sixty),
      "nationalInsuranceNumber" -> optional(nino.verifying(validNinoOnly)),
      "nameOfBenefit" -> nonEmptyText(maxLength = sixty),
      call(routes.G3PersonWhoGetsThisMoney.present())
    )(PersonWhoGetsThisMoney.apply)(PersonWhoGetsThisMoney.unapply))

  def completedQuestionGroups(implicit claim: Claim) = claim.completedQuestionGroups(PersonWhoGetsThisMoney)

  def present = claiming { implicit claim =>
    implicit request =>
      OtherMoney.whenVisible(claim)(() => {
        val currentForm: Form[PersonWhoGetsThisMoney] = claim.questionGroup(PersonWhoGetsThisMoney) match {
          case Some(t: PersonWhoGetsThisMoney) => form.fill(t)
          case _ => form
        }
        Ok(views.html.s8_other_money.g3_personWhoGetsThisMoney(currentForm, completedQuestionGroups))
      })
  }

  def submit = claiming { implicit claim =>
    implicit request =>
      form.bindEncrypted.fold(
        formWithErrors => BadRequest(views.html.s8_other_money.g3_personWhoGetsThisMoney(formWithErrors, completedQuestionGroups)),
        f => claim.update(f) -> Redirect(routes.G4PersonContactDetails.present()))
  }
}