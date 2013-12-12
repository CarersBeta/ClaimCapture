package controllers.circs.s1_identification

import language.reflectiveCalls
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.Controller
import models.view.{Navigable, CachedChangeOfCircs}
import controllers.Mappings._
import models.domain.CircumstancesAboutYou
import utils.helpers.CarersForm._
import controllers.CarersForms._
import play.api.Logger

object G1AboutYou extends Controller with CachedChangeOfCircs with Navigable {

  val title = "title"
  val firstName = "firstName"
  val middleName = "middleName"
  val lastName = "lastName"
  val nationalInsuranceNumber = "nationalInsuranceNumber"
  val dateOfBirth = "dateOfBirth"

  val form = Form(mapping(
    title -> nonEmptyText(maxLength = 4),
    firstName -> carersNonEmptyText(maxLength = 17),
    middleName -> optional(carersText(maxLength = 17)),
    lastName -> carersNonEmptyText(maxLength = Name.maxLength),
    nationalInsuranceNumber -> nino.verifying(filledInNino, validNino),
    dateOfBirth -> dayMonthYear.verifying(validDate)
  )(CircumstancesAboutYou.apply)(CircumstancesAboutYou.unapply))

  def present = newClaim { implicit circs => implicit request => implicit lang =>
    Logger.info(s"Starting new $cacheKey")
    track(CircumstancesAboutYou) { implicit circs => Ok(views.html.circs.s1_identification.g1_aboutYou(form.fill(CircumstancesAboutYou))) }
  }

  def submit = claiming { implicit circs => implicit request => implicit lang =>
    form.bindEncrypted.fold(
      formWithErrors => BadRequest(views.html.circs.s1_identification.g1_aboutYou(formWithErrors)),
      f => circs.update(f) -> Redirect(routes.G2YourContactDetails.present())
    )
  }
}
