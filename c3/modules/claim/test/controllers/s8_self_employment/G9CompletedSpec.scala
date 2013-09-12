package controllers.s8_self_employment

import org.specs2.mutable.{Tags, Specification}
import play.api.test.{FakeRequest, WithApplication}
import models.domain._
import play.api.test.Helpers._
import models.view.CachedDigitalForm

class G9CompletedSpec extends Specification with Tags {
  
  val selfEmploymentInput = Seq("" -> "")
    
  "Self Employment - Controller" should {
    "present 'Completed'" in new WithApplication with Claiming {
      val request = FakeRequest().withSession(CachedDigitalForm.claimKey -> claimKey)

      val result = controllers.s8_self_employment.SelfEmployment.completed(request)
      status(result) mustEqual OK
    }
    
    "redirect to the next page on clicking continue" in new WithApplication with Claiming {
      val request = FakeRequest().withSession(CachedDigitalForm.claimKey -> claimKey)
        .withFormUrlEncodedBody(selfEmploymentInput: _*)

      val result = controllers.s8_self_employment.SelfEmployment.completedSubmit(request)
      status(result) mustEqual SEE_OTHER
    }
  } section("unit", models.domain.SelfEmployment.id)
}