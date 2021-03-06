package controllers.s_about_you

import models.view.{CachedClaim, Navigable}
import play.api.Play._
import play.api.mvc.Controller
import controllers.CarersForms._
import play.api.data.Forms._
import controllers.mappings.Mappings._
import play.api.data.{FormError, Form}
import models.domain.NationalityAndResidency
import gov.dwp.carers.xml.validation.CommonValidation
import utils.helpers.CarersForm._
import play.api.i18n._

object GNationalityAndResidency extends Controller with CachedClaim with Navigable with I18nSupport {
  override val messagesApi: MessagesApi = current.injector.instanceOf[MMessages]

  val form = Form(mapping(
    "nationality" -> nonEmptyText.verifying(NationalityAndResidency.validNationality),
    "actualnationality" -> optional(carersNonEmptyText(maxLength = CommonValidation.NATIONALITY_MAX_LENGTH)),
    "alwaysLivedInUK" -> nonEmptyText.verifying(validYesNo),
    "liveInUKNow" -> optional(text.verifying(validYesNo)),
    "arrivedInUK" -> optional(carersNonEmptyText(maxLength = CommonValidation.NATIONALITY_MAX_LENGTH)),
    "arrivedInUKDate" -> optional(dayMonthYear.verifying(validDate)),
    "arrivedInUKFrom" -> optional(carersNonEmptyText()),
    "trip52weeks" -> nonEmptyText.verifying(validYesNo),
    "tripDetails" -> optional(carersNonEmptyText(maxLength = 3000))
  )(NationalityAndResidency.apply)(NationalityAndResidency.unapply)
    .verifying(NationalityAndResidency.actualNationalityRequired)
    .verifying("liveInUKNow.required", validateLiveInUKnow _)
    .verifying("arrivedInUK.required", validateArrivedInUK _)
    .verifying("arrivedInUKDate.required", validateArrivedInUKDate _)
    .verifying("arrivedInUKFrom.required", validateArrivedInUKFrom _)
    .verifying(NationalityAndResidency.requiredTripDetails)
  )

  private def validateLiveInUKnow(nationalityAndResidency: NationalityAndResidency) = {
    nationalityAndResidency.alwaysLivedInUK match {
      case `no` => nationalityAndResidency.liveInUKNow.isDefined
      case _ => true
    }
  }

  private def validateArrivedInUK(nationalityAndResidency: NationalityAndResidency) = {
    nationalityAndResidency.liveInUKNow match {
      case Some(`yes`) => nationalityAndResidency.arrivedInUK.isDefined
      case _ => true
    }
  }

  private def validateArrivedInUKDate(nationalityAndResidency: NationalityAndResidency) = {
    nationalityAndResidency.arrivedInUK match {
      case Some("less") => nationalityAndResidency.arrivedInUKDate.isDefined
      case _ => true
    }
  }

  private def validateArrivedInUKFrom(nationalityAndResidency: NationalityAndResidency) = {
    nationalityAndResidency.arrivedInUK match {
      case Some("less") => nationalityAndResidency.arrivedInUKFrom.isDefined
      case _ => true
    }
  }

  def present = claimingWithCheck {implicit claim => implicit request => implicit request2lang =>
    track(NationalityAndResidency) { implicit claim =>
      Ok(views.html.s_nationality_and_residency.g_nationalityAndResidency(form.fill(NationalityAndResidency)))
    }
  }

  def submit = claimingWithCheck {implicit claim => implicit request => implicit request2lang =>
    form.bindEncrypted.fold(
      formWithErrors => {
        val formWithErrorsUpdate = formWithErrors
          .replaceError("", "actualnationality.required", FormError("actualnationality", errorRequired))
          .replaceError("", "liveInUKNow.required", FormError("liveInUKNow", errorRequired))
          .replaceError("", "arrivedInUK.required", FormError("arrivedInUK", errorRequired))
          .replaceError("", "arrivedInUKDate.required", FormError("arrivedInUKDate", errorRequired))
          .replaceError("", "arrivedInUKFrom.required", FormError("arrivedInUKFrom", errorRequired))
          .replaceError("", "tripdetails.required", FormError("tripDetails", errorRequired))
        BadRequest(views.html.s_nationality_and_residency.g_nationalityAndResidency(formWithErrorsUpdate))
      },
      nationalityAndResidency => {
        claim.update(nationalityAndResidency) -> Redirect(routes.GPaymentsFromAbraod.present())
      })
  } withPreview()
}
