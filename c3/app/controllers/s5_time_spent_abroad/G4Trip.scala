package controllers.s5_time_spent_abroad

import play.api.mvc.Controller
import models.view.CachedClaim
import play.api.data.Form
import play.api.data.Forms._
import models.domain.{FiftyTwoWeeksTrip, Trip, FourWeeksTrip, Trips}
import utils.helpers.CarersForm._
import play.api.i18n.Messages
import models.DayMonthYear
import TimeSpentAbroad.trips
import controllers.Mappings._
import controllers.CarersForms._
import models.domain.Trip
import scala.Some
import models.domain.Trip
import scala.Some

object G4Trip extends Controller with CachedClaim {
  val form = Form(mapping(
    "tripID" -> nonEmptyText,
    "start" -> (dayMonthYear verifying validDate),
    "end" -> (dayMonthYear verifying validDate),
    "where" -> carersNonEmptyText(maxLength = 35),
    "why" -> carersNonEmptyText
  )(Trip.apply)(Trip.unapply))

  val fourWeeksLabel = "s5.g2"
  val fiftyTwoWeeksLabel = "s5.g3"

  def fourWeeks = claiming { implicit claim => implicit request => implicit lang =>
    Ok(views.html.s5_time_spent_abroad.g4_trip(form, fourWeeksLabel, routes.G4Trip.fourWeeksSubmit(),  routes.G2AbroadForMoreThan4Weeks.present()))
  }

  def fourWeeksSubmit = claiming { implicit claim => implicit request => implicit lang =>
    form.bindEncrypted.fold(
      formWithErrors => BadRequest(views.html.s5_time_spent_abroad.g4_trip(formWithErrors, fourWeeksLabel, routes.G4Trip.fourWeeksSubmit(), routes.G2AbroadForMoreThan4Weeks.present())),
      trip => {
        val updatedTrips = if (trips.fourWeeksTrips.size >= 5) trips else trips.update(trip.as[FourWeeksTrip])
        claim.update(updatedTrips) -> Redirect(routes.G2AbroadForMoreThan4Weeks.present())
      })
  }

  def fiftyTwoWeeks = claiming { implicit claim => implicit request => implicit lang =>
    Ok(views.html.s5_time_spent_abroad.g4_trip(form, fiftyTwoWeeksLabel, routes.G4Trip.fiftyTwoWeeksSubmit(), routes.G3AbroadForMoreThan52Weeks.present()))
  }

  def fiftyTwoWeeksSubmit = claiming { implicit claim => implicit request => implicit lang =>
    form.bindEncrypted.fold(
      formWithErrors => BadRequest(views.html.s5_time_spent_abroad.g4_trip(formWithErrors, fiftyTwoWeeksLabel, routes.G4Trip.fiftyTwoWeeksSubmit(), routes.G3AbroadForMoreThan52Weeks.present())),
      trip => {
        val updatedTrips = if (trips.fiftyTwoWeeksTrips.size >= 5) trips else trips.update(trip.as[FiftyTwoWeeksTrip])
        claim.update(updatedTrips) -> Redirect(routes.G3AbroadForMoreThan52Weeks.present())
      })
  }

  def trip(id: String) = claiming { implicit claim => implicit request => implicit lang =>
    claim.questionGroup(Trips) match {
      case Some(ts: Trips) => ts.fourWeeksTrips.find(_.id == id) match {
        case Some(t: Trip) => Ok(views.html.s5_time_spent_abroad.g4_trip(form.fill(t), fourWeeksLabel, routes.G4Trip.fourWeeksSubmit(), routes.G2AbroadForMoreThan4Weeks.present()))
        case _ => ts.fiftyTwoWeeksTrips.find(_.id == id) match {
          case Some(t: Trip) => Ok(views.html.s5_time_spent_abroad.g4_trip(form.fill(t), fiftyTwoWeeksLabel, routes.G4Trip.fiftyTwoWeeksSubmit(), routes.G3AbroadForMoreThan52Weeks.present()))
          case _ => Redirect(routes.G1NormalResidenceAndCurrentLocation.present())
        }
      }

      case _ => Redirect(routes.G1NormalResidenceAndCurrentLocation.present())
    }
  }

  def delete(id: String) = claiming { implicit claim => implicit request => implicit lang =>
    import play.api.libs.json.Json
    import language.postfixOps

    claim.questionGroup(Trips) match {
      case Some(ts: Trips) => {
        val updatedTrips = ts.delete(id)

        if (updatedTrips.fourWeeksTrips.isEmpty) claim.update(updatedTrips) -> Ok(Json.obj("anyTrips" -> Messages("4Weeks.label", (DayMonthYear.today - 3 years).`dd/MM/yyyy`)))
        else claim.update(updatedTrips) -> Ok(Json.obj("anyTrips" -> Messages("4Weeks.more.label", (DayMonthYear.today - 3 years).`dd/MM/yyyy`)))
      }

      case _ => BadRequest(s"""Failed to delete trip with ID "$id" as claim currently has no trips""")
    }
  }
}