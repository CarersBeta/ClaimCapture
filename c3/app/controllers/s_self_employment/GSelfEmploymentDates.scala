package controllers.s_self_employment

import controllers.CarersForms._
import controllers.mappings.Mappings._
import controllers.s_self_employment.SelfEmployment._
import models.domain.{SelfEmploymentDates, Claim}
import models.view.ClaimHandling.ClaimResult
import models.view.{CachedClaim, Navigable}
import play.api.Play._
import play.api.data.{Form, FormError}
import play.api.data.Forms._
import play.api.i18n._
import play.api.mvc.{AnyContent, Controller, Request}
import utils.helpers.CarersForm._

import scala.language.reflectiveCalls

object GSelfEmploymentDates extends Controller with CachedClaim with Navigable with I18nSupport {
  override val messagesApi: MessagesApi = current.injector.instanceOf[MMessages]

  val form = Form(mapping(
    "stillSelfEmployed" -> carersNonEmptyText.verifying(validYesNo),
    "moreThanYearAgo" -> optional(text.verifying(validYesNo)),
    "startThisWork" -> optional(dayMonthYear.verifying(validDate)),
    "haveAccounts" -> optional(text.verifying(validYesNo)),
    "knowTradingYear" -> optional(text.verifying(validYesNo)),
    "tradingYearStart" -> optional(dayMonthYear.verifying(validDate)),
    "paidMoney" -> optional(text.verifying(validYesNo)),
    "paidMoneyDate" -> optional(dayMonthYear.verifying(validDate)),
    "finishThisWork" -> optional(dayMonthYear.verifying(validDate))
  )(SelfEmploymentDates.apply)(SelfEmploymentDates.unapply)
    .verifying("moreThanYearAgo.required", validateMoreThanYearAgo _)
    .verifying("haveAccounts.required", validateHaveAccounts _)
    .verifying("knowTradingYear.required", validateKnowTradingYear _)
    .verifying("tradingYearStart.required", validateTradingYearStart _)
    .verifying("startThisWork.required", validateStartThisWork _)
    .verifying("paidMoney.required", validatePaidMoney _)
    .verifying("paidMoneyDate.required", validatePaidMoneyDate _)
    .verifying("finishThisWork.required", validateFinishThisWork _)
  )

  private def validateMoreThanYearAgo(selfEmploymentDates: SelfEmploymentDates) = {
    selfEmploymentDates.stillSelfEmployed match {
      case `yes` => selfEmploymentDates.moreThanYearAgo.isDefined
      case _ => true
    }
  }

  private def validateHaveAccounts(selfEmploymentDates: SelfEmploymentDates) = {
    selfEmploymentDates.moreThanYearAgo match {
      case Some(`yes`) => selfEmploymentDates.haveAccounts.isDefined
      case _ => true
    }
  }

  private def validateKnowTradingYear(selfEmploymentDates: SelfEmploymentDates) = {
    selfEmploymentDates.haveAccounts match {
      case Some(`no`) => selfEmploymentDates.knowTradingYear.isDefined
      case _ => true
    }
  }

  private def validateTradingYearStart(selfEmploymentDates: SelfEmploymentDates) = {
    selfEmploymentDates.knowTradingYear match {
      case Some(`yes`) => selfEmploymentDates.tradingYearStart.isDefined
      case _ => true
    }
  }

  private def validateStartThisWork(selfEmploymentDates: SelfEmploymentDates) = {
    selfEmploymentDates.moreThanYearAgo match {
      case Some(`no`) => selfEmploymentDates.startThisWork.isDefined
      case _ => true
    }
  }

  private def validatePaidMoney(selfEmploymentDates: SelfEmploymentDates) = {
    selfEmploymentDates.moreThanYearAgo match {
      case Some(`no`) => selfEmploymentDates.paidMoney.isDefined
      case _ => true
    }
  }

  private def validatePaidMoneyDate(selfEmploymentDates: SelfEmploymentDates) = {
    selfEmploymentDates.paidMoney match {
      case Some(`yes`) => selfEmploymentDates.paidMoneyDate.isDefined
      case _ => true
    }
  }

  private def validateFinishThisWork(selfEmploymentDates: SelfEmploymentDates) = {
    selfEmploymentDates.stillSelfEmployed match {
      case `no` => selfEmploymentDates.finishThisWork.isDefined
      case _ => true
    }
  }

  def present = claimingWithCheck { implicit claim => implicit request => implicit request2lang =>
    presentConditionally(aboutSelfEmployment)
  }

  private def aboutSelfEmployment(implicit claim: Claim, request: Request[AnyContent]): ClaimResult = {
    track(SelfEmploymentDates) {
      implicit claim => Ok(views.html.s_self_employment.g_selfEmploymentDates(form.fill(SelfEmploymentDates)))
    }
  }

  def submit = claimingWithCheck { implicit claim => implicit request => implicit request2lang =>
    form.bindEncrypted.fold(
      formWithErrors => {
        val formWithErrorsUpdate = formWithErrors
          .replaceError("", "moreThanYearAgo.required", FormError("moreThanYearAgo", errorRequired))
          .replaceError("", "startThisWork.required", FormError("startThisWork", errorRequired))
          .replaceError("", "haveAccounts.required", FormError("haveAccounts", errorRequired))
          .replaceError("", "knowTradingYear.required", FormError("knowTradingYear", errorRequired))
          .replaceError("", "tradingYearStart.required", FormError("tradingYearStart", errorRequired))
          .replaceError("", "paidMoney.required", FormError("paidMoney", errorRequired))
          .replaceError("", "paidMoneyDate.required", FormError("paidMoneyDate", errorRequired))
          .replaceError("", "finishThisWork.required", FormError("finishThisWork", errorRequired))
        BadRequest(views.html.s_self_employment.g_selfEmploymentDates(formWithErrorsUpdate))
      },
      f => claim.update(f) -> Redirect(routes.GSelfEmploymentPensionsAndExpenses.present()))
  }
}