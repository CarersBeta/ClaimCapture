package services.submission

import app.ReportChange._
import app.XMLValues._
import scala.concurrent.ExecutionContext
import play.api.{http, Logger}
import controllers.submission._
import models.domain.{ReportChanges, Declaration}
import services.ClaimTransactionComponent
import ExecutionContext.Implicits.global
import play.api.libs.ws.Response
import models.domain.Claim
import scala.Some

trait AsyncClaimSubmissionService {

  this: ClaimTransactionComponent with WebServiceClientComponent =>

  import AsyncClaimSubmissionService._

  def submission(claim: Claim): Unit = {
    val txnID = claim.transactionId.get
    Logger.info(s"Retrieved Id : $txnID")


    webServiceClient.submitClaim(claim, txnID).map(
      response => {
        claimTransaction.registerId(txnID, SUBMITTED, claimType(claim))
        recordMi(claim, txnID)
        processResponse(claim, txnID, response)
      }
    )
  }

  private def ok(claim: Claim, txnID: String, response: Response) = {
    val responseStr = response.body
    Logger.info(s"Received response : ${claim.key} : $responseStr")
    val responseXml = scala.xml.XML.loadString(responseStr)
    val result = (responseXml \\ "result").text
    Logger.info(s"Received result : ${claim.key} : $result")
    result match {
      case "response" =>
        claimTransaction.updateStatus(txnID, SUCCESS, claimType(claim))
      case "acknowledgement" =>
        claimTransaction.updateStatus(txnID, ACKNOWLEDGED, claimType(claim))
      case "error" =>
        val errorCode = (responseXml \\ "errorCode").text
        Logger.error(s"Received error : $result, TxnId : $txnID, Error code : $errorCode")
        claimTransaction.updateStatus(txnID, errorCode, claimType(claim))
      case _ =>
        Logger.error(s"Received error : $result, TxnId : $txnID, Error code : $UNKNOWN_ERROR")
        claimTransaction.updateStatus(txnID, UNKNOWN_ERROR, claimType(claim))
    }
  }

  private def processResponse(claim: Claim, txnID: String, response: Response): Unit = {
    response.status match {
      case http.Status.OK => ok(claim,txnID,response)

      case http.Status.SERVICE_UNAVAILABLE =>
        Logger.error(s"SERVICE_UNAVAILABLE : ${response.status} : ${response.toString}, TxnId : $txnID")
        claimTransaction.updateStatus(txnID, SERVICE_UNAVAILABLE, claimType(claim))
      case http.Status.BAD_REQUEST =>
        Logger.error(s"BAD_REQUEST : ${response.status} : ${response.toString}, TxnId : $txnID")
        claimTransaction.updateStatus(txnID, BAD_REQUEST_ERROR, claimType(claim))
      case http.Status.REQUEST_TIMEOUT =>
        Logger.error(s"REQUEST_TIMEOUT : ${response.status} : ${response.toString}, TxnId : $txnID")
        claimTransaction.updateStatus(txnID, REQUEST_TIMEOUT_ERROR, claimType(claim))
      case http.Status.INTERNAL_SERVER_ERROR =>
        Logger.error(s"INTERNAL_SERVER_ERROR : ${response.status} : ${response.toString}, TxnId : $txnID")
        claimTransaction.updateStatus(txnID, SERVER_ERROR, claimType(claim))
    }
  }

  private def recordMi(claim: Claim, id: String): Unit = {
    val changesMap = Map(StoppedCaring.name -> Some(0), AddressChange.name -> Some(1), SelfEmployment.name -> Some(2), PaymentChange.name -> Some(3), AdditionalInfo.name -> Some(4), NotAsked -> None)
    val declaration = claim.questionGroup[Declaration].getOrElse(Declaration())
    val thirdParty = declaration.someoneElse.isDefined
    val circsChange = changesMap(claim.questionGroup[ReportChanges].getOrElse(ReportChanges()).reportChanges)
    claimTransaction.recordMi(id, thirdParty, circsChange, claim.lang)
  }


}

object AsyncClaimSubmissionService{
  val SUBMITTED = "0000"
  val ACKNOWLEDGED = "0001"
  val SUCCESS = "0002"
  val UNKNOWN_ERROR = "9001"
  val BAD_REQUEST_ERROR = "9002"
  val REQUEST_TIMEOUT_ERROR = "9003"
  val SERVER_ERROR = "9004"
  val COMMUNICATION_ERROR = "9005"
  val SERVICE_UNAVAILABLE = "9006"
}