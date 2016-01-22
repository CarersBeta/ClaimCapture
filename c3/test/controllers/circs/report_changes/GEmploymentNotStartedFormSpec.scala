package controllers.circs.report_changes

import utils.WithApplication
import org.specs2.mutable._
import models.DayMonthYear

class GEmploymentNotStartedFormSpec extends Specification {
  val yes = "yes"
  val no = "no"
  val amountPaid = "£199.98"
  val whenExpectedToBePaidDateDay = 10
  val whenExpectedToBePaidDateMonth = 11
  val whenExpectedToBePaidDateYear = 2012
  val weekly = "weekly"
  val monthly = "monthly"
  val monthlyPayDay = "2nd Thursday every month"
  val other = "other"
  val otherText = "some other text"
  val willYouPayIntoPensionText = "pension text"
  val willYouPayForThingsText = "Some things needed to do the job"
  val moreInfo = "more information"

  section("unit", models.domain.CircumstancesSelfEmployment.id)
  "Report an Employment change in your circumstances where employment has not started - Employment Form" should {
    "map weekly paid with no pension/expenses paid when been paid set to 'yes'" in new WithApplication {
      GEmploymentNotStarted.form.bind(
        Map(
          "beenPaidYet" -> yes,
          "howMuchPaid" -> amountPaid,
          "whenExpectedToBePaidDate.day" -> whenExpectedToBePaidDateDay.toString,
          "whenExpectedToBePaidDate.month" -> whenExpectedToBePaidDateMonth.toString,
          "whenExpectedToBePaidDate.year" -> whenExpectedToBePaidDateYear.toString,
          "howOften.frequency" -> weekly,
          "usuallyPaidSameAmount" -> no,
          "willYouPayIntoPension.answer" -> no,
          "willYouPayForThings.answer" -> no,
          "willCareCostsForThisWork.answer" -> no
        )
      ).fold(
          formWithErrors => {
            "This mapping should not happen." must equalTo("Error")
          },
          f => {
            f.beenPaid must equalTo(yes)
            f.whenExpectedToBePaidDate.get must equalTo(DayMonthYear(Some(whenExpectedToBePaidDateDay), Some(whenExpectedToBePaidDateMonth), Some(whenExpectedToBePaidDateYear), None, None))
            f.howOften.frequency must equalTo(weekly)
            f.usuallyPaidSameAmount.get must equalTo(no)
            f.payIntoPension.answer must equalTo(no)
            f.willYouPayForThings.answer must equalTo(no)
            f.careCostsForThisWork.answer must equalTo(no)
          }
        )
    }

    "should fail for special characters" in new WithApplication {
      GEmploymentNotStarted.form.bind(
        Map(
          "beenPaidYet" -> yes,
          "howMuchPaid" -> amountPaid,
          "whenExpectedToBePaidDate.day" -> whenExpectedToBePaidDateDay.toString,
          "whenExpectedToBePaidDate.month" -> whenExpectedToBePaidDateMonth.toString,
          "whenExpectedToBePaidDate.year" -> whenExpectedToBePaidDateYear.toString,
          "howOften.frequency" -> weekly,
          "usuallyPaidSameAmount" -> no,
          "willYouPayIntoPension.answer" -> yes,
          "willYouPayIntoPension.whatFor" -> willYouPayIntoPensionText.concat("%"),
          "willYouPayForThings.answer" -> yes,
          "willYouPayForThings.whatFor" -> willYouPayForThingsText.concat("%"),
          "willCareCostsForThisWork.answer" -> yes,
          "willCareCostsForThisWork.whatCosts" -> "1% of the money earned"
        )
      ).fold(
        formWithErrors => {
          formWithErrors.errors.size mustEqual 3
        },
        f => {
          "This mapping should not happen." must equalTo("Error")
        }
      )
    }
  }
  section("unit", models.domain.CircumstancesSelfEmployment.id)
}