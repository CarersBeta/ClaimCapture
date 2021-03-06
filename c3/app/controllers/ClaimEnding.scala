package controllers

import play.api.Play._
import play.api.mvc.Controller
import models.view.CachedClaim
import app.ConfigProperties._
import services.EmailServices
import models.domain.{ContactDetails, PreviewModel}
import play.api.i18n._

object ClaimEnding extends Controller with CachedClaim with I18nSupport {
  override val messagesApi: MessagesApi = current.injector.instanceOf[MMessages]
  def timeout = endingOnError {implicit claim => implicit request => implicit request2lang =>
    RequestTimeout(views.html.common.session_timeout(startPage))
  }

  def error = endingOnError {implicit claim => implicit request => implicit request2lang =>
    InternalServerError(views.html.common.error(startPage))
  }

  def errorCookie = endingOnError {implicit claim => implicit request => implicit request2lang =>
    Unauthorized(views.html.common.error_cookie_retry(startPage))
  }

  def errorBrowserBackbutton = endingOnError {implicit claim => implicit request => implicit request2lang =>
    BadRequest(views.html.common.errorBrowserBackbutton(startPage))
  }

  def thankyou = ending {implicit claim => implicit request => implicit request2lang =>
    Ok(views.html.common.thankYouClaim(request2lang))
  }

}
