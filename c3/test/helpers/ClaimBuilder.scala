package helpers

import models.domain._
import models._
import domain.Break
import scala.Some
import models.Whereabouts
import models.MultiLineAddress
import yesNo.{YesNoWithDate, YesNoWithDropDownAndText, YesNoWithDropDown}

object ClaimBuilder {
  val yourDetails = YourDetails(title = "mr", firstName = "Phil", middleName = None, surname = "Smith",
    otherSurnames = Some("O'Dwyer"), None, nationality = "French",
    dateOfBirth = DayMonthYear(1, 1, 1963), maritalStatus = "m", alwaysLivedUK = "yes")

  val contactDetails = ContactDetails(address = MultiLineAddress(Some("Line1"), None, None),
    postcode = Some("PR2 8AE"),
    phoneNumber = Some("01772 700806"), None)

  val timeOutsideUK = TimeOutsideUK(livingInUK = LivingInUK("yes", Some(DayMonthYear()), Some(""), Some(YesNoWithDate("yes", Some(DayMonthYear())))), visaReference = None)

  val claimDate = ClaimDate(dateOfClaim = DayMonthYear(1, 1, 2013))

  val aboutYou = AboutYou(yourDetails, contactDetails, Some(timeOutsideUK), claimDate)

  val theirPersonalDetails = TheirPersonalDetails(title = "ms", firstName = "Minnie", middleName = None, surname = "Mouse",
    None, dateOfBirth = DayMonthYear(1, 1, 1963), liveAtSameAddress = "no")

  val theirContactDetails = TheirContactDetails(address = MultiLineAddress(Some("Line1"), None, None), postcode = Some("PR2 8AE"))

  val moreAboutThePerson = MoreAboutThePerson(relationship = "mother", None, claimedAllowanceBefore = "no")

  val representatives = RepresentativesForPerson(youAct = YesNoWithDropDown("yes", Some("Lawyer")), someoneElseAct = YesNoWithDropDownAndText("yes",
    Some("Lawyer"), Some("Mr. Lawyer")))

  val previousCarerContactDetails = PreviousCarerContactDetails(address = Some(MultiLineAddress(Some("Line1"), None, None)), postcode = Some("PR2 8AE"))

  val previousCarerPersonalDetails = PreviousCarerPersonalDetails(firstName = Some("Some"), middleName = None, surname = Some("One"),
    None, dateOfBirth = Some(DayMonthYear(1, 1, 1963)))

  val moreAboutTheCare = MoreAboutTheCare(spent35HoursCaring = "yes", spent35HoursCaringBeforeClaim = YesNoWithDate("no", Some(DayMonthYear(1, 1, 2013))), hasSomeonePaidYou = "yes")

  val oneWhoPays = OneWhoPaysPersonalDetails(organisation = Some("SomeOrg Inc."), amount = Some("300"), startDatePayment = Some(DayMonthYear(1, 1, 2012)))

  val contactDetailsPayingPerson = ContactDetailsOfPayingPerson(address = Some(MultiLineAddress(Some("Line1"), None, None)), postcode = Some("PR2 8AE"))

  val breaksInCare = BreaksInCare(
    List(
      Break(id = "1", start = DayMonthYear(1, 1, 2001), end = Some(DayMonthYear(1, 5, 2001)), whereYou = Whereabouts("Holiday", None), wherePerson = Whereabouts("Hospital", None), medicalDuringBreak = "yes"),
      Break(id = "1", start = DayMonthYear(1, 1, 2002), end = Some(DayMonthYear(1, 5, 2002)), whereYou = Whereabouts("Holiday", None), wherePerson = Whereabouts("Hospital", None), medicalDuringBreak = "yes")
    )
  )


  val careYouProvide = CareYouProvide(theirPersonalDetails, theirContactDetails,
    moreAboutThePerson, representatives,
    Some(previousCarerContactDetails), Some(previousCarerPersonalDetails),
    moreAboutTheCare, Some(oneWhoPays), Some(contactDetailsPayingPerson), breaksInCare)

  val yourPartnerPersonalDetails = YourPartnerPersonalDetails(title = "mr", firstName = "Michael", middleName = None, surname = "Mouse", otherNames = Some("Oswald"), nationalInsuranceNumber = Some(NationalInsuranceNumber(Some("AA"), Some("12"), Some("34"), Some("56"), Some("A"))), dateOfBirth = DayMonthYear(1, 1, 1930), nationality = Some("British"), liveAtSameAddress = "yes")
  val yourPartnerContactDetails = YourPartnerContactDetails(address = Some(MultiLineAddress(Some("Line1"), None, None)), postcode = Some("PR2 8AE"))
  val moreAboutYourPartner = MoreAboutYourPartner(startedLivingTogether = Some(YesNoWithDate("yes", Some(DayMonthYear(1, 1, 1940)))), separated = YesNoWithDate("no",None))
  val personYouCareFor = PersonYouCareFor(isPartnerPersonYouCareFor = "yes")
  val yourPartner = YourPartner(yourPartnerPersonalDetails, yourPartnerContactDetails,
    moreAboutYourPartner, Some(personYouCareFor))

  val howWePayYou = HowWePayYou("01","everyWeek")
  val bank = BankBuildingSocietyDetails("Holder","Bank name",SortCode("12","34","56"),"1234567890","1234")

  val payDetails = PayDetails(howWePayYou,bank)

  val additionalInfo = AdditionalInfo(Some("Other information"),"yes")
  val consent = Consent("no",Some("I don't want to"),"no",Some("I said I don't want to"))
  val disclaimer = Disclaimer("checked")
  val declaration = Declaration("checked",Some("checked"))

  val consentAndDeclaration = ConsentAndDeclaration(additionalInfo,consent,disclaimer,declaration)
}
