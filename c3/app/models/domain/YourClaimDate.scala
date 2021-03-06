package models.domain

import controllers.mappings.Mappings
import models.DayMonthYear
import models.yesNo.YesNoWithDate
import play.api.i18n.Lang
import utils.helpers.HtmlLabelHelper.displayPlaybackDatesFormat
import scala.language.postfixOps


object YourClaimDate extends Identifier(id = "s2")

case class ClaimDate(dateOfClaim: DayMonthYear = DayMonthYear(), spent35HoursCaringBeforeClaim:YesNoWithDate = YesNoWithDate("", None)) extends QuestionGroup(ClaimDate) {

  def dateWeRequireBreakInCareInformationFrom(lang: Lang): String = {

    def getCorrectDate(dateOfClaim: DayMonthYear, caringStartDate: DayMonthYear, spent35HoursCaringBeforeClaim: Boolean) = {
      if (!spent35HoursCaringBeforeClaim)
        dateOfClaim
      else {
        if ((dateOfClaim - 6 months) isBefore caringStartDate)
          caringStartDate
        else
          dateOfClaim - 6 months
      }
    }

    val spent35HoursCaring = if (spent35HoursCaringBeforeClaim.answer == Mappings.yes) true else false
    val caringStartDate = spent35HoursCaringBeforeClaim.date.getOrElse(dateOfClaim)
    val date = getCorrectDate(dateOfClaim, caringStartDate, spent35HoursCaring)
    displayPlaybackDatesFormat(lang, date)
  }

}

object ClaimDate extends QGIdentifier(id = s"${YourClaimDate.id}.g1")

