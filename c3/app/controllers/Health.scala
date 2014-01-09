package controllers

import play.api.mvc.{Controller, Action}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

trait HealthController {
  this: Controller =>

  def health = Action {
    val now = DateTimeFormat.forPattern("dd-MM-yy HH:mm:ss").print(DateTime.now())
    Ok(views.html.common.health(now))
  }
}

object HealthController extends Controller with HealthController
