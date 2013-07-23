package controllers.s3_your_partner

import language.reflectiveCalls
import controllers.Mappings._
import models.domain.YourPartnerPersonalDetails
import models.view.CachedClaim
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.Controller
import utils.helpers.CarersForm.formBinding

object G1YourPartnerPersonalDetails extends Controller with CachedClaim {
  val form = Form(
    mapping(
      call(routes.G1YourPartnerPersonalDetails.present()),
      "title" -> nonEmptyText(maxLength = 4),
      "firstName" -> nonEmptyText(maxLength = sixty),
      "middleName" -> optional(text(maxLength = sixty)),
      "surname" -> nonEmptyText(maxLength = sixty),
      "otherNames" -> optional(text(maxLength = sixty)),
      "nationalInsuranceNumber" -> optional(nino.verifying(validNinoOnly)),
      "dateOfBirth" -> dayMonthYear.verifying(validDate),
      "nationality" -> optional(text(maxLength = sixty)),
      "liveAtSameAddress" -> nonEmptyText.verifying(validYesNo)
    )(YourPartnerPersonalDetails.apply)(YourPartnerPersonalDetails.unapply))


  def present = claiming { implicit claim => implicit request =>
    YourPartner.whenVisible(claim)(Ok(views.html.s3_your_partner.g1_yourPartnerPersonalDetails(form.fill(YourPartnerPersonalDetails))))
  }

  def submit = claiming { implicit claim => implicit request =>
    form.bindEncrypted.fold(
      formWithErrors => BadRequest(views.html.s3_your_partner.g1_yourPartnerPersonalDetails(formWithErrors)),
      f => claim.update(f) -> Redirect(routes.G2YourPartnerContactDetails.present()))
  }
}