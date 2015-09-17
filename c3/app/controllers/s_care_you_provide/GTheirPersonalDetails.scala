package controllers.s_care_you_provide

import controllers.mappings.AddressMappings._
import controllers.mappings.Mappings
import models.yesNo.YesNoMandWithAddress
import play.api.Logger

import language.reflectiveCalls
import play.api.data.{FormError, Form}
import play.api.data.Forms._
import play.api.mvc.Controller
import controllers.mappings.Mappings._
import models.view.{Navigable, CachedClaim}
import utils.helpers.CarersForm._
import models.domain._
import controllers.CarersForms._
import models.DayMonthYear
import controllers.mappings.NINOMappings._

object GTheirPersonalDetails extends Controller with CachedClaim with Navigable {

  val addressMapping = "theirAddress"->mapping(
    "answer" -> nonEmptyText.verifying(validYesNo),
    "address" -> optional(address.verifying(requiredAddress)),
    "postCode" -> optional(text verifying validPostcode)
      )(YesNoMandWithAddress.apply)(YesNoMandWithAddress.unapply)

  val form = Form(mapping(
    "relationship" -> carersNonEmptyText(maxLength = 35),
    "title" -> carersNonEmptyText(maxLength = Mappings.five),
    "titleOther" -> optional(carersText(maxLength = Mappings.twenty)),
    "firstName" -> carersNonEmptyText(maxLength = 17),
    "middleName" -> optional(carersText(maxLength = 17)),
    "surname" -> carersNonEmptyText(maxLength = Name.maxLength),
    "nationalInsuranceNumber" -> optional(nino.verifying(validNino)),
    "dateOfBirth" -> dayMonthYear.verifying(validDate),
    addressMapping
  )(TheirPersonalDetails.apply)(TheirPersonalDetails.unapply)
    .verifying("titleOther.required", TheirPersonalDetails.verifyTitleOther _)
    .verifying("theirAddress.address", validateSameAddressAnswer _)
  )

  private def validateSameAddressAnswer(form: TheirPersonalDetails) = form.theirAddress.answer match {
      case `no` => form.theirAddress.address.isDefined
      case _ => true
    }


  def present = claimingWithCheck { implicit claim => implicit request => lang =>
    val isPartnerPersonYouCareFor = YourPartner.visible &&
      claim.questionGroup[YourPartnerPersonalDetails].exists(_.isPartnerPersonYouCareFor.getOrElse("") == "yes")

    Logger.info(claim.navigation.toString)

    val currentForm = if (isPartnerPersonYouCareFor) {
      claim.questionGroup(YourPartnerPersonalDetails) match {
        case Some(t: YourPartnerPersonalDetails) =>
          val theirPersonalDetails = claim.questionGroup(TheirPersonalDetails).getOrElse(TheirPersonalDetails()).asInstanceOf[TheirPersonalDetails]
          form.fill(TheirPersonalDetails(relationship = theirPersonalDetails.relationship,
            title = t.title.getOrElse(""),
            titleOther = t.titleOther,
            firstName = t.firstName.getOrElse(""),
            middleName = t.middleName,
            surname = t.surname.getOrElse(""),
            nationalInsuranceNumber = t.nationalInsuranceNumber,
            dateOfBirth = t.dateOfBirth.getOrElse(DayMonthYear(None, None, None)),
            theirAddress = theirPersonalDetails.theirAddress
          )) // Pre-populate form with values from YourPartnerPersonalDetails - this is for the case that the Caree is your partner
        case _ => form // Blank form (user can only get here if they skip sections by manually typing URL).
      }
    } else {
      form.fill(TheirPersonalDetails)
    }

    track(TheirPersonalDetails) { implicit claim => Ok(views.html.s_care_you_provide.g_theirPersonalDetails(currentForm)(lang)) }
  }

  def submit = claimingWithCheck { implicit claim => implicit request => lang =>
    form.bindEncrypted.fold(
      formWithErrors => {
        val updatedFormWithErrors = formWithErrors
          .replaceError("", "titleOther.required", FormError("titleOther", "constraint.required"))
          .replaceError("","theirAddress.address", FormError("theirAddress.address", errorRequired))

        BadRequest(views.html.s_care_you_provide.g_theirPersonalDetails(updatedFormWithErrors)(lang))
      },
      theirPersonalDetails => {
        val liveAtSameAddress = theirPersonalDetails.theirAddress.answer == yes

        //copy the address from the carer
        val updatedTheirPersonalDetails = if(liveAtSameAddress){
          claim.questionGroup[ContactDetails].map{ cd =>
            theirPersonalDetails.copy(theirAddress = YesNoMandWithAddress(answer = yes, address= Some(cd.address), postCode = cd.postcode))
          }.getOrElse(theirPersonalDetails)
        }else{
          theirPersonalDetails
        }

        claim.update(updatedTheirPersonalDetails) -> Redirect(routes.GMoreAboutTheCare.present())
      })
  } withPreview()
}