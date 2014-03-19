package services

import play.api.db.DB
import play.api.Play.current
import anorm._
import play.api.Play

trait ClaimTransactionComponent {
  val claimTransaction : ClaimTransaction

  class ClaimTransaction {
    /**
     * Generate a new unique ID
     */
    def generateId: String = {
      DB.withConnection("carers") {
        connection =>
          try {
            val statement = connection.prepareCall("select get_new_transaction_id();")
            statement.execute()
            val result = statement getResultSet()
            result.next
            result.getString("get_new_transaction_id")
          }
          catch {
            case e: java.lang.Exception => throw new UnavailableTransactionIdException("Cannot generate an unique transaction ID.", e)
          }
      }
    }

    /**
     * Record that an ID has been used
     */
    def registerId(id: String, statusCode:String, claimType:Int,thirdParty:Boolean=false, circsChange:Option[Int]=None):Unit = DB.withConnection("carers") {implicit c =>
      SQL(
        """
          INSERT INTO transactionstatus (transaction_id, status,type,thirdparty,circs_type)
          VALUES ({transactionId},{status},{type},{thirdparty},{circsChange});
        """
      ).on("transactionId"->id,"status"->statusCode,"type"->claimType,"thirdparty"->(if(thirdParty) 1 else 0),"circsChange"->circsChange).execute()
    }


    def updateStatus(id: String, statusCode:String, claimType:Int):Unit = DB.withConnection("carers") {implicit connection =>
      SQL(
        """
            UPDATE transactionstatus set status={status}, type={type}
            WHERE transaction_id={transactionId};
        """
      ).on("status"->statusCode,"type"->claimType,"transactionId"->id).executeUpdate()
    }


  }

  class StubClaimTransaction extends ClaimTransaction {
    override def generateId: String = "TEST623"

    override def registerId(id: String, statusCode: String, claimType: Int, thirdParty:Boolean, circsChange:Option[Int]) {}

    override def updateStatus(id: String, statusCode: String, claimType: Int) {}
  }

}

/**
 * Exception thrown by UniqueTransactionId if it could not generate an id. The cause is described by the nested exception.
 * @param message  the detail message
 * @param nestedException  the cause
 */
class UnavailableTransactionIdException(message: String, nestedException: Exception)
  extends RuntimeException(message, nestedException) {}
