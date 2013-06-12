package controllers

import org.specs2.mutable.Specification
import play.api.test.{WithApplication, FakeRequest}
import play.api.test.Helpers._

import play.api.cache.Cache
import models.claim.Benefits
import models.claim.Hours
import models.claim.Section
import models.claim.LivesInGB
import models.claim.Over16
import models.claim.Claim
import scala.Some
import utils.ClaimUtils

class CarersAllowanceSpec extends Specification {
  """Can you get Carer's Allowance""" should {
    "start with a new Claim" in new WithApplication with Claiming {
      val request = FakeRequest().withSession("connected" -> claimKey)
      val result = CarersAllowance.benefits(request)

      header(CACHE_CONTROL, result) must beSome("no-cache, no-store")

      Cache.getAs[Claim](claimKey) must beLike {
        case Some(c: Claim) => c.sections.size mustEqual 0
      }
    }

    "acknowledge that the person looks after get one of the required benefits" in new WithApplication with Claiming {
      val request = FakeRequest().withFormUrlEncodedBody("answer" -> "true").withSession("connected" -> claimKey)
      CarersAllowance.benefitsSubmit(request)

      val claim = Cache.getAs[Claim](claimKey).get
      val section: Section = claim.section(CarersAllowance.id).get

      section.form(Benefits.id) must beLike {
        case Some(f: Benefits) => f.answer mustEqual true
      }
    }

    "acknowledge that the person looks after does not get one of the required benefits " in new WithApplication with Claiming {
      val request = FakeRequest().withFormUrlEncodedBody("answer" -> "false").withSession("connected" -> claimKey)
      CarersAllowance.benefitsSubmit(request)

      val claim = Cache.getAs[Claim](claimKey).get
      val section: Section = claim.section(CarersAllowance.id).get

      section.form(Benefits.id) must beLike {
        case Some(f: Benefits) => f.answer mustEqual false
      }
    }

    "present the hours form" in new WithApplication with Claiming {
      val request = FakeRequest().withSession("connected" -> claimKey)

      val claim = Claim().update(Benefits(answer = true))
      Cache.set(claimKey, claim)

      val result = CarersAllowance.hours(request)

      status(result) mustEqual OK

      val sectionId = ClaimUtils.sectionId(Benefits.id)
      val answeredForms = claim.completedFormsForSection(sectionId).dropWhile(_.id != Benefits.id)
      answeredForms(0) mustEqual Benefits(answer = true)
    }

    "acknowledge that you spend 35 hours or more each week caring for the person you look after" in new WithApplication with Claiming {
      val request = FakeRequest().withFormUrlEncodedBody("answer" -> "true").withSession("connected" -> claimKey)
      CarersAllowance.hoursSubmit(request)
      val claim = Cache.getAs[Claim](claimKey).get
      val section: Section = claim.section(CarersAllowance.id).get

      section.form(Hours.id) must beLike {
        case Some(f: Hours) => f.answer mustEqual true
      }
    }

    """acknowledge that the person looks after get one of the required benefits AND (proving that previous steps are cached)
       acknowledge that you spend 35 hours or more each week caring for the person you look after""" in new WithApplication with Claiming {
      val benefitsRequest = FakeRequest().withFormUrlEncodedBody("answer" -> "true").withSession("connected" -> claimKey)
      CarersAllowance.benefitsSubmit(benefitsRequest)

      val hoursRequest = FakeRequest().withFormUrlEncodedBody("answer" -> "true").withSession("connected" -> claimKey)
      CarersAllowance.hoursSubmit(hoursRequest)
      val claim = Cache.getAs[Claim](claimKey).get
      val section: Section = claim.section(CarersAllowance.id).get

      section.forms.size mustEqual 2

      section.form(Hours.id) must beLike {
        case Some(f: Hours) => f.answer mustEqual true
      }
    }

    "present the lives in GB form" in new WithApplication with Claiming {
      val request = FakeRequest().withSession("connected" -> claimKey)

      val claimWithBenefitFrom = Claim().update(Benefits(answer = true))
      val claimWithHoursForm = claimWithBenefitFrom.update(Hours(answer = true))
      Cache.set(claimKey, claimWithHoursForm)

      val result = CarersAllowance.hours(request)

      status(result) mustEqual OK

      val sectionId = ClaimUtils.sectionId(LivesInGB.id)
      val answeredForms = claimWithHoursForm.completedFormsForSection(sectionId)

      answeredForms(0) mustEqual Benefits(answer = true)
      answeredForms(1) mustEqual Hours(answer = true)
    }

    "acknowledge that carer lives in Great Britain" in new WithApplication with Claiming {
      val request = FakeRequest().withFormUrlEncodedBody("answer" -> "true").withSession("connected" -> claimKey)
      CarersAllowance.livesInGBSubmit(request)

      val claim = Cache.getAs[Claim](claimKey).get
      val section: Section = claim.section(CarersAllowance.id).get

      section.form(LivesInGB.id) must beLike {
        case Some(f: LivesInGB) => f.answer mustEqual true
      }
    }

    "present the Are you aged 16 or over form" in new WithApplication with Claiming {
      val request = FakeRequest().withSession("connected" -> claimKey)

      val claimWithBenefit = Claim().update(Benefits(answer = true))
      val claimWithHours = claimWithBenefit.update(Hours(answer = true))
      val claimWithLivesInGB = claimWithHours.update(LivesInGB(answer = true))
      Cache.set(claimKey, claimWithLivesInGB)

      val result = CarersAllowance.hours(request)

      status(result) mustEqual OK

      val sectionId = ClaimUtils.sectionId(Over16.id)
      val answeredForms = claimWithLivesInGB.completedFormsForSection(sectionId)

      answeredForms(0) mustEqual Benefits(answer = true)
      answeredForms(1) mustEqual Hours(answer = true)
      answeredForms(2) mustEqual LivesInGB(answer = true)
    }

    "acknowledge that carer is aged 16 or over" in new WithApplication with Claiming {
      val request = FakeRequest().withFormUrlEncodedBody("answer" -> "true").withSession("connected" -> claimKey)
      CarersAllowance.over16Submit(request)

      val claim = Cache.getAs[Claim](claimKey).get
      val section: Section = claim.section(CarersAllowance.id).get

      section.form(Over16.id) must beLike {
        case Some(f: Over16) => f.answer mustEqual true
      }
    }

    "acknowledge that the carer is eligible for allowance" in new WithApplication with Claiming {
      val request = FakeRequest().withSession("connected" -> claimKey)

      val claim = Claim().update(Benefits(answer = true))
        .update(Hours(answer = true))
        .update(LivesInGB(answer = true))
        .update(Over16(answer = true))

      Cache.set(claimKey, claim)

      val result = CarersAllowance.approve(request)
      contentAsString(result) must contain("div class=\"prompt\"")
    }

    "note that the carer is not eligible for allowance" in new WithApplication with Claiming {
      val request = FakeRequest().withSession("connected" -> claimKey)

      val claim = Claim().update(Benefits(answer = true))
        .update(Hours(answer = true))
        .update(LivesInGB(answer = false))
        .update(Over16(answer = true))

      Cache.set(claimKey, claim)

      val result = CarersAllowance.approve(request)

      contentAsString(result) must contain("div class=\"prompt error\"")
    }
  }
}