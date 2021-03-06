package services.submission

import java.util.UUID._
import app.ConfigProperties._
import models.domain.{Claim, _}
import models.view.{CachedChangeOfCircs, CachedClaim}
import models.{DayMonthYear, NationalInsuranceNumber}
import org.specs2.mock.Mockito
import org.specs2.mutable._
import play.api.{Logger, http}
import play.api.libs.ws.WSResponse
import play.api.test.FakeApplication
import services._
import scala.concurrent.Future

class AsyncClaimSubmissionServiceSpec extends Specification with Mockito {

  val ni = "AB123456D"

  def resultXml(result: String, correlationID: String, messageClass:String, errorCode: String, pollEndpoint: String) = {
    <response>
      <result>{result}</result>
      <correlationID>{correlationID}</correlationID>
      <messageClass>{messageClass}</messageClass>
      <pollEndpoint>{pollEndpoint}</pollEndpoint>
      <errorCode>{errorCode}</errorCode>
    </response>
  }

  def asyncService( status:Int=200, transactionId:String, result: String= "",
                    correlationID: String="", messageClass:String="",
                    errorCode: String="", pollEndpoint: String ="") = new AsyncClaimSubmissionService with ClaimTransactionComponent
                                                                                                      with WebServiceClientComponent {
      import scala.concurrent.ExecutionContext.Implicits.global
      val webServiceClient = mock[WebServiceClient]
      val response = mock[WSResponse]
      response.status returns status
      response.body returns resultXml(result,correlationID,messageClass,errorCode,pollEndpoint).buildString(stripComments = false)

      webServiceClient.submitClaim(any[Claim],any[String]) returns Future(response)
      val claimTransaction = new ClaimTransaction
      val submissionCacheService = mock[SubmissionCacheService]
      submissionCacheService.getFromCache(any[Claim]) returns Some("test")
  }

  def getClaim(surname: String): Claim = {
    val claim = new Claim(CachedClaim.key, transactionId = Some(transactionId), uuid=randomUUID.toString)

    // need to set the qs groups used to create the fingerprint of the claim, otherwise a dup cache error will be thrown
    val det = new YourDetails("Mr","",None, surname,NationalInsuranceNumber(Some(ni)), DayMonthYear(Some(1), Some(1), Some(1969)))

    val claimDate = new ClaimDate(DayMonthYear(Some(1), Some(1), Some(2014)))

    claim + det + claimDate match {
      case c:Claim => new Claim(c.key, c.sections, c.created, c.lang, c.gacid, c.uuid, c.transactionId)(c.navigation)
    }

  }

  def getCofc(firstName: String, surname: String): Claim = {
    val claim = new Claim(CachedChangeOfCircs.key,transactionId = Some(transactionId), uuid=randomUUID.toString)

    // need to set the qs groups used to create the fingerprint of the claim, otherwise a dup cache error will be thrown
    val det = new CircumstancesYourDetails(firstName, surname, NationalInsuranceNumber(Some(ni)), DayMonthYear(Some(1), Some(1), Some(1967)), "", None, None, "", "")

    val claimDate = new ClaimDate(DayMonthYear(Some(1), Some(1), Some(2014)))

    claim + det + claimDate match {
      case c:Claim =>
        Logger.info(s"getCofc ${c.key}")
        new Claim(c.key, c.sections, c.created, c.lang, c.gacid, c.uuid, c.transactionId)(c.navigation)
    }
  }

  val transactionId = "1234567"

  section ("unit", "slow")
  "claim submission" should {
    "record BAD_REQUEST" in new WithApplicationAndDB {

      val service = asyncService(http.Status.BAD_REQUEST,transactionId)

      val claim = new Claim(CachedClaim.key,transactionId = Some(transactionId))

      serviceSubmission(service, claim)

      Thread.sleep(500)
      val transactionStatus = service.claimTransaction.getTransactionStatusById(transactionId)

      transactionStatus mustEqual Some(TransactionStatus(transactionId,ClaimSubmissionService.BAD_REQUEST_ERROR,1,Some(0),None,Some("en")))
    }

    "record SUCCESS" in new WithApplicationAndDB {
      val service = asyncService(http.Status.OK,transactionId,result = "response")

      serviceSubmission(service, getClaim("test"))

      Thread.sleep(500)
      val transactionStatus = service.claimTransaction.getTransactionStatusById(transactionId)

      transactionStatus mustEqual Some(TransactionStatus(transactionId,ClaimSubmissionService.SUCCESS,1,Some(0),None,Some("en")))
    }

    "record change of circs submission SUCCESS" in new WithApplicationAndDB {
      val service = asyncService(http.Status.OK,transactionId,result = "response")

      serviceSubmission(service, getCofc("test", "jones"))

      Thread.sleep(500)
      val transactionStatus = service.claimTransaction.getTransactionStatusById(transactionId)

      transactionStatus mustEqual Some(TransactionStatus(transactionId,ClaimSubmissionService.SUCCESS,2,Some(0),None,Some("en")))
    }

    "do not submit a duplicate claim" in new WithApplicationAndDB(Map("mailer.enabled"->"false")) {
      val service = asyncService(http.Status.OK,transactionId)
      val claim = getClaim("test")
      service.storeInCache(claim)

      serviceSubmission(service, claim)
      Thread.sleep(1500)
      val transactionStatus = service.claimTransaction.getTransactionStatusById(transactionId)

      transactionStatus mustEqual Some(TransactionStatus(transactionId,ClaimSubmissionService.INTERNAL_ERROR,1,None,None,None))
    }

    "do not submit a duplicate change of circs" in new WithApplicationAndDB(Map("mailer.enabled"->"false")) {
      val service = asyncService(http.Status.OK,transactionId)
      val claim = getCofc("john", "jones")
      service.storeInCache(claim)

      serviceSubmission(service, claim)
      Thread.sleep(1500)
      val transactionStatus = service.claimTransaction.getTransactionStatusById(transactionId)

      transactionStatus mustEqual Some(TransactionStatus(transactionId,ClaimSubmissionService.INTERNAL_ERROR,2,None,None,None))
    }

    "record SERVICE_UNAVAILABLE" in new WithApplicationAndDB {
      val service = asyncService(http.Status.SERVICE_UNAVAILABLE,transactionId)

      val claim = getClaim("test1")

      serviceSubmission(service, claim)

      Thread.sleep(1000)
      val transactionStatus = service.claimTransaction.getTransactionStatusById(transactionId)

      transactionStatus mustEqual Some(TransactionStatus(transactionId,ClaimSubmissionService.SERVICE_UNAVAILABLE,1,Some(0),None,Some("en")))
    }

    "record REQUEST_TIMEOUT_ERROR" in new WithApplicationAndDB {
      val service = asyncService(http.Status.REQUEST_TIMEOUT,transactionId)

      val claim = getClaim("test2")

      serviceSubmission(service, claim)

      Thread.sleep(500)
      val transactionStatus = service.claimTransaction.getTransactionStatusById(transactionId)

      transactionStatus mustEqual Some(TransactionStatus(transactionId,ClaimSubmissionService.REQUEST_TIMEOUT_ERROR,1,Some(0),None,Some("en")))
    }

    "record SERVER_ERROR" in new WithApplicationAndDB {
      val service = asyncService(http.Status.INTERNAL_SERVER_ERROR,transactionId)

      val claim = getClaim("test3")

      serviceSubmission(service, claim)

      Thread.sleep(500)
      val transactionStatus = service.claimTransaction.getTransactionStatusById(transactionId)

      transactionStatus mustEqual Some(TransactionStatus(transactionId,ClaimSubmissionService.SERVER_ERROR,1,Some(0),None,Some("en")))
    }
  }
  section ("unit", "slow")

  def serviceSubmission(service: AsyncClaimSubmissionService with ClaimTransactionComponent, claim: Claim)(implicit app: FakeApplication) {
    DBTests.createId(transactionId)
    service.claimTransaction.registerId(transactionId, ClaimSubmissionService.SUBMITTED, controllers.submission.claimType(claim), 1, getStringProperty("origin.tag"))
    service.submission(claim)
  }
}
