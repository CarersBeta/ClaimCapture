package controllers.s7_employment

import org.specs2.mutable.{Tags, Specification}
import play.api.test.{FakeRequest, WithApplication}
import play.api.test.Helpers._
import models.view.CachedClaim
import play.api.cache.Cache
import models.domain.{Claim, EmployerContactDetails, Job, Jobs, JobDetails, Claiming}

class G1EmploymentSpec extends Specification with Tags {
  "Employment" should {
    "get first completed question group for a job" in new WithApplication with Claiming {
      val jobID = "dummyJobID"

      val jobDetails = JobDetails(jobID)
      val job = Job(jobID).update(jobDetails)
      val jobs = new Jobs().update(job)

      val claim = Claim().update(jobs)
      Cache.set(claimKey, claim)

      val request = FakeRequest().withSession(CachedClaim.key -> claimKey)
      val completedQuestionGroups = Employment.completedQuestionGroups(EmployerContactDetails, jobID)(claim)
    }

    "issue an 'error' when deleting a non-existing job" in new WithApplication with Claiming {
      val result = Employment.delete("nonExistingJobID")(FakeRequest().withSession(CachedClaim.key -> claimKey))
      status(result) shouldEqual BAD_REQUEST
    }
  } section("unit", models.domain.Employed.id)
}