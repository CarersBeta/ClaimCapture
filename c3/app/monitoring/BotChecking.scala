package monitoring

import app.ConfigProperties._
import models.domain.Claimable
import models.domain.Claim
import scala.Some
import play.api.Logger
import jmx.inspectors.FastSubmissionNotifier

trait BotChecking extends FastSubmissionNotifier {

  def checkTimeToCompleteAllSections(claimOrCircs: Claim with Claimable, currentTime: Long): Boolean

  def honeyPot(claim: Claim): Boolean

  def isSpeedBot(claimOrCircs: Claim): Boolean = {
    val checkForBotSpeed = getProperty("checkForBotSpeed", default = false)
    checkForBotSpeed && checkTimeToCompleteAllSections(claimOrCircs, System.currentTimeMillis())
  }

  def isHoneyPotBot(claimOrCircs: Claim): Boolean = {
    val checkForBotHoneyPot = getProperty("checkForBotHoneyPot", default = false)
    checkForBotHoneyPot && honeyPot(claimOrCircs)
  }

  def evaluateTimeToCompleteAllSections(claim: Claim with Claimable, currentTime: Long = System.currentTimeMillis(), sectionExpectedTimes: Map[String, Long]) = {

    val expectedMinTimeToCompleteAllSections: Long = claim.sections.map(s => {
      sectionExpectedTimes.get(s.identifier.id) match {
        case Some(n) =>
          if (s.questionGroups.size > 0) n else 0
        case _ => 0
      }
    }).reduce(_ + _) // Aggregate all of the sectionExpectedTimes for completed sections only.

    val actualTimeToCompleteAllSections: Long = currentTime - claim.created

    val result = actualTimeToCompleteAllSections < expectedMinTimeToCompleteAllSections

    if (result) {
      fireFastNotification(claim)
      Logger.error(s"Detected bot completing sections too quickly! actualTimeToCompleteAllSections: $actualTimeToCompleteAllSections < expectedMinTimeToCompleteAllSections: $expectedMinTimeToCompleteAllSections")
    }
    result
  }


}