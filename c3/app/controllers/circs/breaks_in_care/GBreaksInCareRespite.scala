package controllers.circs.breaks_in_care

import controllers.CarersForms._
import controllers.IterationID
import controllers.mappings.Mappings
import controllers.mappings.Mappings._
import models.domain._
import models.view.{CachedChangeOfCircs, CachedClaim}
import play.api.Play._
import play.api.data.Forms._
import play.api.data.{Form, FormError}
import play.api.i18n.{I18nSupport, MMessages, MessagesApi}
import play.api.mvc.{Controller, Request}
import utils.helpers.CarersForm._

object GBreaksInCareRespite extends Controller with CachedChangeOfCircs with I18nSupport with BreaksGatherChecks {
  override val messagesApi: MessagesApi = current.injector.instanceOf[MMessages]

  val yourStayEndedMapping = "yourRespiteStayEnded" -> optional(yesNoWithDate)

  val dpStayEndedMapping = "dpRespiteStayEnded" -> optional(yesNoWithDate)

  val form = Form(mapping(
    "iterationID" -> carersNonEmptyText,
    "typeOfCare" -> default(carersNonEmptyText, Breaks.carehome),
    "whoWasInRespite" -> carersNonEmptyText.verifying(validWhoWasAwayType),
    "whenWereYouAdmitted" -> optional(dayMonthYear),
    yourStayEndedMapping,
    "whenWasDpAdmitted" -> optional(dayMonthYear),
    dpStayEndedMapping,
    "breaksInCareRespiteStillCaring" -> optional(nonEmptyText),
    "yourMedicalProfessional" -> optional(nonEmptyText),
    "dpMedicalProfessional" -> optional(nonEmptyText),
    "caringEnded" -> default(optional(dayMonthYear), None),
    "caringStarted" -> default(optional(yesNoWithDate), None),
    "whereWasDp" -> default(optional(radioWithText), None),
    "whereWereYou" -> default(optional(radioWithText), None),
    "caringEnded.time" -> default(optional(carersNonEmptyText), None),
    "caringStarted.time" -> default(optional(carersNonEmptyText), None)
  )(CircsBreak.apply)(CircsBreak.unapply)
    .verifying(requiredWhenWereYouAdmitted)
    .verifying(requiredYourStayEndedAnswer)
    .verifying(requiredYourStayEndedDate)
    .verifying(requiredWhenWasDpAdmitted)
    .verifying(requiredDpStayEndedAnswer)
    .verifying(requiredDpStayEndedDate)
    .verifying(requiredBreaksInCareStillCaring)
    .verifying(requiredStartDateNotAfterEndDate)
    .verifying(requiredMedicalProfessional)
  )

  //need to go back to summary if any breaks exist
  val backCall = routes.GBreaksInCareSummary.present()

  def present(iterationID: String) = claimingWithCheck { implicit circs => implicit request => implicit request2lang =>
    val break = circs.questionGroup[CircsBreaksInCare].getOrElse(CircsBreaksInCare(List())).breaks.find(_.iterationID == iterationID).getOrElse(CircsBreak())
    Ok(views.html.circs.breaks_in_care.breaksInCareRespite(form.fill(break), backCall))
  }

  def submit = claimingWithCheck { implicit circs => implicit request => implicit request2lang =>
    form.bindEncrypted.fold(
      formWithErrors => {
        val dp = dpDetails(circs);
        val formWithErrorsUpdate = formWithErrors
          .replaceError("", "whenWereYouAdmitted", FormError("whenWereYouAdmitted", errorRequired))
          .replaceError("", "whenWereYouAdmitted.invalid", FormError("whenWereYouAdmitted", errorInvalid))
          .replaceError("", "yourStayEnded.answer", FormError("yourRespiteStayEnded.answer", errorRequired))
          .replaceError("", "yourStayEnded.date", FormError("yourRespiteStayEnded.date", errorRequired))
          .replaceError("", "yourStayEnded.date.invalid", FormError("yourRespiteStayEnded.date", errorInvalid))
          .replaceError("", "yourStayEnded.invalidDateRange", FormError("yourRespiteStayEnded.date", errorInvalidDateRange))
          .replaceError("", "yourMedicalProfessional", FormError("yourMedicalProfessional", errorRequired))
          .replaceError("", "yourMedicalProfessional.invalidYesNo", FormError("yourMedicalProfessional", invalidYesNo))
          .replaceError("", "whenWasDpAdmitted", FormError("whenWasDpAdmitted", errorRequired, Seq(dp)))
          .replaceError("", "whenWasDpAdmitted.invalid", FormError("whenWasDpAdmitted", errorInvalid, Seq(dp)))
          .replaceError("", "dpStayEnded.answer", FormError("dpRespiteStayEnded.answer", errorRequired))
          .replaceError("", "dpStayEnded.date", FormError("dpRespiteStayEnded.date", errorRequired, Seq(dp)))
          .replaceError("", "dpStayEnded.date.invalid", FormError("dpRespiteStayEnded.date", errorInvalid, Seq(dp)))
          .replaceError("", "dpStayEnded.invalidDateRange", FormError("dpRespiteStayEnded.date", errorInvalidDateRange, Seq(dp)))
          .replaceError("", "dpMedicalProfessional", FormError("dpMedicalProfessional", errorRequired, Seq(dp)))
          .replaceError("", "dpMedicalProfessional.invalidYesNo", FormError("dpMedicalProfessional", invalidYesNo, Seq(dp)))
          .replaceError("", "breaksInCareStillCaring", FormError("breaksInCareRespiteStillCaring", errorRequired, Seq(dp)))
          .replaceError("", "breaksInCareStillCaring.invalidYesNo", FormError("breaksInCareRespiteStillCaring", invalidYesNo, Seq(dp)))
        BadRequest(views.html.circs.breaks_in_care.breaksInCareRespite(formWithErrorsUpdate, backCall))
      },
      break => {
        val updatedBreaksInCare = breaksInCare.update(break).breaks.size match {
          case noOfBreaks if (noOfBreaks > CircsBreaks.maximumBreaks) => breaksInCare
          case _ => breaksInCare.update(break)
        }
        val updatedClaim = updateClaim(updatedBreaksInCare)
        updatedClaim -> Redirect(nextPage)
      })
  }

  private def updateClaim(newbreaks: CircsBreaksInCare)(implicit circs: Claim) = {
    val updatedBreaks = updatedBreakTypesObject
    val updatedClaim = circs.update(updatedBreaks).update(newbreaks)
    updatedClaim
  }

  private def updatedBreakTypesObject(implicit circs: Claim) = {
    // Delete the carehome answer from claim. Otherwise, it will prepopulate the answer when return to Summary page
    // But if we are the last break type being collected, clear all answers so that summary page needs to be re-selected.
    def breaksTypes(implicit claim: Claim) = claim.questionGroup[CircsBreaksInCareType].getOrElse(CircsBreaksInCareType())
    if (breaksTypes.hospital.isDefined || breaksTypes.other.equals(Some(Mappings.yes))) {
      breaksTypes.copy(carehome = None)
    }
    else {
      new BreaksInCareType()
    }
  }

  //either other or if come from summary back to there
  private def nextPage(implicit circs: Claim, request: Request[_]) = {
    val breaksInCareType = circs.questionGroup(CircsBreaksInCareType).getOrElse(CircsBreaksInCareType()).asInstanceOf[CircsBreaksInCareType]
    breaksInCareType.other.isDefined match {
      case true if (breaksInCareType.other.get == Mappings.yes) => routes.GBreaksInCareOther.present(IterationID(form))
      case _ => routes.GBreaksInCareSummary.present()
    }
  }

  def breaksInCare(implicit circs: Claim) = circs.questionGroup[CircsBreaksInCare].getOrElse(CircsBreaksInCare())
  def breaksTypes(implicit circs: Claim) = circs.questionGroup[CircsBreaksInCareType].getOrElse(CircsBreaksInCareType())
}
