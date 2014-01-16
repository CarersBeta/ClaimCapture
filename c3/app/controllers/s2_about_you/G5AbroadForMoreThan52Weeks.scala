package controllers.s2_about_you

import play.api.mvc.Controller
import play.api.data.Form
import play.api.data.Forms._
import controllers.Mappings._
import models.domain.AbroadForMoreThan52Weeks
import AboutYou.trips
import models.view.Navigable
import utils.helpers.CarersForm._
import models.view.CachedClaim

object G5AbroadForMoreThan52Weeks extends Controller with CachedClaim with Navigable {
  val form = Form(mapping(
    "anyTrips" -> nonEmptyText.verifying(validYesNo)
  )(AbroadForMoreThan52Weeks.apply)(AbroadForMoreThan52Weeks.unapply))

  def present = claiming { implicit claim => implicit request => implicit lang =>
    val `52WeeksForm` = claim.questionGroup[AbroadForMoreThan52Weeks].map(form.fill).getOrElse(form)
    track(AbroadForMoreThan52Weeks) { implicit claim => Ok(views.html.s2_about_you.g5_abroad_for_more_than_52_weeks(`52WeeksForm`, trips)) }
  }

  def submit = claiming { implicit claim => implicit request => implicit lang =>
    def next(abroadForMoreThan52Weeks: AbroadForMoreThan52Weeks) = abroadForMoreThan52Weeks.anyTrips match {
      case `yes` if trips.fiftyTwoWeeksTrips.size < 5 => Redirect(routes.G6Trip.present())
      case `yes` => Redirect(routes.G5AbroadForMoreThan52Weeks.present())
      case _ => Redirect(routes.G7OtherEEAStateOrSwitzerland.present())
    }

    form.bindEncrypted.fold(
      formWithErrors => BadRequest(views.html.s2_about_you.g5_abroad_for_more_than_52_weeks(formWithErrors, trips)),
      abroadForMoreThan52Weeks => claim.update(abroadForMoreThan52Weeks).update(trips) -> next(abroadForMoreThan52Weeks))
  }
}