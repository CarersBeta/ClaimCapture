package controllers.breaks_in_care

import controllers.IterationID
import controllers.mappings.Mappings
import controllers.mappings.Mappings._
import controllers.s_care_you_provide.GMoreAboutTheCare._
import models.domain._
import models.yesNo.DeleteId
import play.api.Play._
import play.api.data.Forms._
import play.api.i18n._
import play.api.mvc.{Request, Controller}
import play.api.data.{Form, FormError}
import play.api.Logger
import utils.helpers.CarersForm._
import models.view.{CachedClaim}

object GBreaksInCareSummary extends Controller with CachedClaim with I18nSupport {
  override val messagesApi: MessagesApi = current.injector.instanceOf[MMessages]

  def form(implicit claim: Claim) = Form(mapping(
    "breaktype_hospital" -> optional(nonEmptyText),
    "breaktype_carehome" -> optional(nonEmptyText),
    "breaktype_none" -> optional(nonEmptyText),
    "breaktype_other" -> optional(text.verifying(validYesNo))
  )(BreaksInCareType.apply)(BreaksInCareType.unapply)
    .verifying("selectother", validateOther _)
    .verifying("deselectnone", validateAnySelected _)
    .verifying("selectone", validateNoneNotallowed _)
    .verifying("toomanybreaks", validateMaxReached(claim, _))
  )

  def breaks(implicit claim: Claim) = claim.questionGroup[BreaksInCare].getOrElse(BreaksInCare())

  def present = claimingWithCheck { implicit claim => implicit request => implicit request2lang =>
    track(BreaksInCareType) { implicit claim => Ok(views.html.breaks_in_care.breaksInCareSummary(form.fill(BreaksInCareType), breaks)) }
  }

  def submit = claiming { implicit claim => implicit request => implicit request2lang =>
    form.bindEncrypted.fold(
      formWithErrors => {
        val errors = formWithErrors
          .replaceError("", "toomanybreaks", FormError("breaks.toomanybreaks", "breaks.toomanybreaks", Seq(Breaks.maximumBreaks)))
          .replaceError("", "deselectnone", FormError("breaktype", "breaks.breaktype.deselectnone", Seq(dateForBreaks(claim, request2lang), anyothertimes, dpname(claim))))
          .replaceError("", "selectone", FormError("breaktype", "breaks.breaktype.selectone", Seq(dateForBreaks(claim, request2lang), anyothertimes, dpname(claim))))
          .replaceError("", "selectother", FormError("breaktype_other", errorRequired, Seq(dpname(claim))))
        BadRequest(views.html.breaks_in_care.breaksInCareSummary(errors, breaks))
      },
      breaksInCareSummary => {
        claim.update(breaksInCareSummary) -> Redirect(nextPage(breaksInCareSummary))
      })
  }

  private def otherbreakLabel(implicit claim: Claim) = breaks.hasBreaks match {
    case false => "breaktype_other_first"
    case true => "breaktype_other_another"
  }

  private def nextPage(breaksInCareType: BreaksInCareType)(implicit claim: Claim, request: Request[_]): String = {
    if (breaksInCareType.hospital.isDefined) routes.GBreaksInCareHospital.present(IterationID(form)).url
    else if (breaksInCareType.carehome.isDefined) routes.GBreaksInCareRespite.present(IterationID(form)).url
    else if (breaksInCareType.other.equals(Some(Mappings.yes))) routes.GBreaksInCareOther.present(IterationID(form)).url
    else if (claim.navigation.beenInPreview) (controllers.preview.routes.Preview.present().url + getReturnToSummaryValue(claim))
    else controllers.s_education.routes.GYourCourseDetails.present.url
  }

  private def dpname(claim: Claim) = {
    val theirPersonalDetails = claim.questionGroup(TheirPersonalDetails).getOrElse(TheirPersonalDetails()).asInstanceOf[TheirPersonalDetails]
    theirPersonalDetails.firstName + " " + theirPersonalDetails.surname
  }

  private def anyothertimes()(implicit claim: Claim) = (breaks.hasBreaksForType(Breaks.hospital), breaks.hasBreaksForType(Breaks.carehome)) match {
    case (true, _) => Messages("breaktype.anyothertimes")
    case (_, true) => Messages("breaktype.anyothertimes")
    case _ => Messages("breaktype.anytimes")
  }

  private def dateForBreaks(claim: Claim, lang: Lang) = {
    val claimDateQG = claim.questionGroup[ClaimDate].getOrElse(ClaimDate())
    claimDateQG.dateWeRequireBreakInCareInformationFrom(lang)
  }

  private def validateAnySelected(breaksInCareType: BreaksInCareType) = breaksInCareType match {
    case BreaksInCareType(None, None, None, _) => false
    case _ => true
  }

  private def validateNoneNotallowed(breaksInCareType: BreaksInCareType) = someTrue match {
    case (breaksInCareType.hospital | breaksInCareType.carehome) if (breaksInCareType.none == someTrue) => false
    case _ => true
  }

  private def validateOther(breaksInCareType: BreaksInCareType) = breaksInCareType.other match {
    case Some(Mappings.yes) => true
    case Some(Mappings.no) => true
    case _ => false
  }

  private def validateMaxReached(implicit claim: Claim, breaksInCareType: BreaksInCareType) = {
    Logger.info("Validating breaksInCare Maximum with " + breaksInCare.breaks.size + " against maximum of " + Breaks.maximumBreaks)
    (breaksInCare.maximumReached, breaksInCareType.none, breaksInCareType.other) match {
      case (false, _, _) => true
      case (true, Mappings.someTrue, Some(Mappings.no)) => true
      case _ => false
    }
  }

  val deleteForm = Form(mapping(
    "deleteId" -> nonEmptyText
  )(DeleteId.apply)(DeleteId.unapply))

  def delete = claimingWithCheck {
    implicit claim => implicit request => implicit request2lang =>
      deleteForm.bindEncrypted.fold(
        errors => BadRequest(views.html.breaks_in_care.breaksInCareSummary(form.fill(BreaksInCareType), breaks)),
        deleteForm => {
          val updatedBreaks = breaks.delete(deleteForm.id)
          if (updatedBreaks.breaks == breaks.breaks) BadRequest(views.html.breaks_in_care.breaksInCareSummary(form.fill(BreaksInCareSummary), breaks))
          else claim.update(updatedBreaks).delete(BreaksInCareSummary) -> Redirect(routes.GBreaksInCareSummary.present)
        }
      )
  }

  def breaksInCare(implicit claim: Claim) = claim.questionGroup[BreaksInCare].getOrElse(BreaksInCare())
}
