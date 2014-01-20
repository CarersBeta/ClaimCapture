package xml.circumstances

import org.specs2.mutable.{Tags, Specification}
import models.domain._
import models.{DayMonthYear, MultiLineAddress, NationalInsuranceNumber}

class IdentificationSpec extends Specification with Tags {
  val nationalInsuranceNr = NationalInsuranceNumber(Some("VO"), Some("12"), Some("34"), Some("56"), Some("D"))


  val yourDetails = CircumstancesAboutYou(title = "Mr",
    firstName = "Phil",
    middleName = Some("Joe"),
    lastName = "Smith",
    nationalInsuranceNumber = nationalInsuranceNr,
    dateOfBirth = DayMonthYear(1, 1, 1963))

  val contactDetails = CircumstancesYourContactDetails(address = MultiLineAddress(Some("Line1")),
    postcode = Some("PR2 8AE"),
    phoneNumber = "01772 700806",
    mobileNumber = Some("01818 118181"))

  "Identification" should {
    "generate Claimant xml from a given circumstances" in {
      val claim = Claim().update(yourDetails).update(contactDetails)
      val xml = ClaimantDetails.xml(claim)

      (xml \\ "Surname").text shouldEqual yourDetails.lastName
      (xml \\ "OtherNames").text shouldEqual s"${yourDetails.firstName} ${yourDetails.middleName.get}"
      (xml \\ "Title").text shouldEqual yourDetails.title
      (xml \\ "DateOfBirth").text shouldEqual yourDetails.dateOfBirth.`dd-MM-yyyy`
      (xml \\ "NationalInsuranceNumber").text shouldEqual nationalInsuranceNr.stringify
      (xml \\ "Address" \\ "PostCode").text shouldEqual contactDetails.postcode.get
      (xml \\ "DayTimePhoneNumber").text shouldEqual contactDetails.phoneNumber
      (xml \\ "MobileNumber").text shouldEqual contactDetails.mobileNumber.get
    }

  } section "unit"
}