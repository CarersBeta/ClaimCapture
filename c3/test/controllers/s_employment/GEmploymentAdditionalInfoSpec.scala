package controllers.s_employment

import org.specs2.mutable._
import play.api.test.FakeRequest
import models.domain.Claiming
import play.api.test.Helpers._
import models.view.CachedClaim
import utils.WithApplication

class GEmploymentAdditionalInfoSpec extends Specification {
  val employmentAdditionalInfoInput = Seq("empAdditionalInfo.answer" -> "yes", "empAdditionalInfo.text" -> "I do not have much info")
  val employmentAdditionalInfoInputNoText = Seq("empAdditionalInfo.answer" -> "yes")
  val employmentAdditionalInfoInputNo = Seq("empAdditionalInfo.answer" -> "no")

  section("unit", models.domain.EmploymentAdditionalInfo.id)
  "Employment" should {
    "present Additional information" in new WithApplication with Claiming {
      val request = FakeRequest().withSession(CachedClaim.key -> claimKey)
      val result = GEmploymentAdditionalInfo.present(request)
      status(result) mustEqual OK
    }

    "not submit when no input" in new WithApplication with Claiming {
      val request = FakeRequest().withSession(CachedClaim.key -> claimKey)

      val result = GEmploymentAdditionalInfo.submit(request)
      status(result) mustEqual BAD_REQUEST
    }

    "not submit when answered yes and no text" in new WithApplication with Claiming {
      val request = FakeRequest().withSession(CachedClaim.key -> claimKey).withFormUrlEncodedBody(employmentAdditionalInfoInputNoText : _*)

      val result = GEmploymentAdditionalInfo.submit(request)
      status(result) mustEqual BAD_REQUEST
    }

    "submit with valid input when answered yes" in new WithApplication with Claiming {
      val request = FakeRequest().withSession(CachedClaim.key -> claimKey).withFormUrlEncodedBody(employmentAdditionalInfoInput: _*)

      val result = GEmploymentAdditionalInfo.submit(request)
      status(result) mustEqual SEE_OTHER
    }

    "submit with valid input when answered no" in new WithApplication with Claiming {
      val request = FakeRequest().withSession(CachedClaim.key -> claimKey).withFormUrlEncodedBody(employmentAdditionalInfoInputNo: _*)

      val result = GEmploymentAdditionalInfo.submit(request)
      status(result) mustEqual SEE_OTHER
    }
  }
  section("unit", models.domain.EmploymentAdditionalInfo.id)
}
