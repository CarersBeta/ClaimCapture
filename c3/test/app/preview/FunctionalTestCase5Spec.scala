package app.preview

import utils.WithJsBrowser
import utils.pageobjects.s_eligibility.GBenefitsPage
import utils.pageobjects._
import app.FunctionalTestCommon
import utils.pageobjects.preview.PreviewPage

class FunctionalTestCase5Spec extends FunctionalTestCommon {
  isolated

  section ("functional","preview")
  "The application Claim" should {
    "Successfully run absolute Claim Test Case 5" in new WithJsBrowser with PageObjects {
      import Data.displaying

      val page = GBenefitsPage(context)
      implicit val claim = TestData.readTestDataFromFile("/functional_scenarios/ClaimScenario_TestCase5.csv")
      page goToThePage()
      val lastPage = page runClaimWith(claim, PreviewPage.url)

      val toFindData = Data.build(
        "Name"              displays ("AboutYouTitle","AboutYouFirstName","AboutYouMiddleName","AboutYouSurname"),
        "Date of birth"     displays DateTransformer("AboutYouDateOfBirth"),
        "Address"           displays (AddressTransformer("AboutYouAddress"),"AboutYouPostcode"),
        "Claim date"   displays DateTransformer("ClaimDateWhenDoYouWantYourCarersAllowanceClaimtoStart"),
        "Your nationality"  displays "AboutYouNationalityAndResidencyNationality",
        "Have you always lived in England, Scotland or Wales?"  displays "AboutYouNationalityAndResidencyAlwaysLivedInUK",
        "Have you been away from England, Scotland or Wales for more than 52 weeks in the 3 years before your claim date?"  displays "AboutYouNationalityAndResidencyTrip52Weeks",
        "Have you or any of your close family worked abroad or been paid benefits from outside the United Kingdom since your claim date?"          displays "OtherMoneyOtherEEAGuardQuestion",
        "Name"            displays ("AboutYourPartnerTitle","AboutYourPartnerFirstName","AboutYourPartnerMiddleName","AboutYourPartnerSurname"),
        "Date of birth"   displays DateTransformer("AboutYourPartnerDateofBirth"),
        "Have you separated since your claim date?" displays "AboutYourPartnerHaveYouSeparatedfromYourPartner",
        "Name"            displays ("AboutTheCareYouProvideTitlePersonCareFor","AboutTheCareYouProvideFirstNamePersonCareFor","AboutTheCareYouProvideMiddleNamePersonCareFor","AboutTheCareYouProvideSurnamePersonCareFor"),
        "Date of birth"   displays DateTransformer("AboutTheCareYouProvideDateofBirthPersonYouCareFor"),
        "What's their relationship to you?"                               displays "AboutTheCareYouProvideWhatTheirRelationshipToYou",
        "Do you spend 35 hours or more each week caring for this person?" displays "AboutTheCareYouProvideDoYouSpend35HoursorMoreEachWeek",
        "Have you had any breaks from caring for this person since"               displays (AnyYesTransformer("AboutTheCareYouProvideHaveYouHadAnyMoreBreaksInCare"),NumDetailsProvidedTransformer("AboutTheCareYouProvideHaveYouHadAnyMoreBreaksInCare")),
        "Have you been on a course of education since your claim date?"   displays "EducationHaveYouBeenOnACourseOfEducation",
        "Have you been employed at any time since"      displays "EmploymentHaveYouBeenEmployedAtAnyTime_0",
        "Have you been self-employed at any time since" displays "EmploymentHaveYouBeenSelfEmployedAtAnyTime"
      )

      toFindData.assertReview(claim, context) must beTrue
    }
  }
  section ("functional","preview")
}



