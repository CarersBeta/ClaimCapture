package controllers.s4_care_you_provide

import language.reflectiveCalls
import models.domain._
import play.api.data.Form
import play.api.data.Forms._
import controllers.Mappings._
import play.api.mvc.Controller
import models.view.{Navigable, CachedClaim}
import utils.helpers.CarersForm._

object G1TheirPersonalDetails extends Controller with CachedClaim with Navigable {
  val form = Form(mapping(
    "title" -> nonEmptyText(maxLength = 4),
    "firstName" -> nonEmptyText(maxLength = Name.maxLength),
    "middleName" -> optional(text(maxLength = Name.maxLength)),
    "surname" -> nonEmptyText(maxLength = Name.maxLength),
    "nationalInsuranceNumber" -> optional(nino.verifying(validNino)),
    "dateOfBirth" -> dayMonthYear.verifying(validDate),
    "liveAtSameAddressCareYouProvide" -> nonEmptyText.verifying(validYesNo)
  )(TheirPersonalDetails.apply)(TheirPersonalDetails.unapply))

  def present = claiming { implicit claim => implicit request =>
    //val isPartnerPersonYouCareForX = claim.isSectionVisible(models.domain.YourPartner) && claim.questionGroup[PersonYouCareFor]

    val isPartnerPersonYouCareFor: Boolean = if (claim.isSectionVisible(models.domain.YourPartner)) {
      claim.questionGroup(PersonYouCareFor) match {
        case Some(t: PersonYouCareFor) => t.isPartnerPersonYouCareFor == "yes" // Get value the user selected previously.
        case _ => false
      }
    } else { false }

    val currentForm = if (isPartnerPersonYouCareFor) {
      claim.questionGroup(YourPartnerPersonalDetails) match {
        case Some(t: YourPartnerPersonalDetails) =>
          form.fill(TheirPersonalDetails(title = t.title, firstName = t.firstName, middleName = t.middleName, surname = t.surname,
                                         nationalInsuranceNumber = t.nationalInsuranceNumber,
                                         dateOfBirth = t.dateOfBirth)) // Pre-populate form with values from YourPartnerPersonalDetails
        case _ => form // Blank form (user can only get here if they skip sections by manually typing URL).
      }
    } else {
      form.fill(TheirPersonalDetails)
    }

    track(TheirPersonalDetails) { implicit claim => Ok(views.html.s4_care_you_provide.g1_theirPersonalDetails(currentForm)) }
  }

  def submit = claiming { implicit claim => implicit request =>
    form.bindEncrypted.fold(
      formWithErrors => BadRequest(views.html.s4_care_you_provide.g1_theirPersonalDetails(formWithErrors)),
      theirPersonalDetails => claim.update(theirPersonalDetails) -> Redirect(routes.G2TheirContactDetails.present()))
  }
}