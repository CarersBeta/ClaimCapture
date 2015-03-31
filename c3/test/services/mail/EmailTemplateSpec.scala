package services.mail

import models.DayMonthYear
import models.domain.{ClaimDate, Claim}
import models.view.CachedClaim
import org.specs2.mutable.{Tags, Specification}
import play.api.i18n.Lang
import play.api.i18n.{MMessages => Messages}
import play.api.test.WithApplication
import play.twirl.api.{HtmlFormat, Html}


/**
 * Created by valtechuk on 24/03/2015.
 */
class EmailTemplateSpec extends Specification with Tags {

  def escapeMessage(id:String,param:String="") = HtmlFormat.escape(Messages(id,param)).toString
  "Email template" should {

    "Display claim email" in new WithApplication(){
      val claim = Claim(CachedClaim.key)
      implicit val lang = Lang("en")
      val renderedEmail = views.html.mail(claim,isClaim = true,isEmployment = false).body

      renderedEmail.size must beGreaterThan(0)


      renderedEmail must contain(escapeMessage("mail.claim.title"))
      renderedEmail must contain(escapeMessage("mail.claim.successful"))
      renderedEmail must contain(escapeMessage("mail.claim.next.line1"))
      renderedEmail must contain(escapeMessage("mail.next.line"))

      renderedEmail must not contain escapeMessage("mail.cofc.title")
      renderedEmail must not contain escapeMessage("mail.cofc.successful")
    }

    "Display claim employment email" in new WithApplication(){
      val claim = Claim(CachedClaim.key).+(ClaimDate(DayMonthYear()))

      implicit val lang = Lang("en")
      val renderedEmail = views.html.mail(claim,isClaim = true,isEmployment = true).body

      renderedEmail.size must beGreaterThan(0)


      renderedEmail must contain(escapeMessage("mail.claim.title"))
      renderedEmail must contain(escapeMessage("mail.claim.successful"))
      renderedEmail must contain(escapeMessage("mail.claim.next.line1.alt"))
      renderedEmail must contain(escapeMessage("mail.next.line2"))
      renderedEmail must contain(escapeMessage("mail.next.send1"))
      renderedEmail must contain(escapeMessage("mail.claim.next.send2",DayMonthYear().`dd month yyyy`))

      renderedEmail must not contain escapeMessage("mail.next.line")

      renderedEmail must not contain escapeMessage("mail.cofc.title")
      renderedEmail must not contain escapeMessage("mail.cofc.successful")
    }

    "Display cofc employment email" in new WithApplication(){
      val claim = Claim(CachedClaim.key)
      implicit val lang = Lang("en")
      val renderedEmail = views.html.mail(claim,isClaim = false,isEmployment = true).body

      renderedEmail.size must beGreaterThan(0)


      renderedEmail must contain(escapeMessage("mail.cofc.title"))
      renderedEmail must contain(escapeMessage("mail.cofc.successful"))
      renderedEmail must contain(escapeMessage("mail.cofc.next.send2"))

      renderedEmail must contain(escapeMessage("mail.next.send1"))
      renderedEmail must contain(escapeMessage("mail.next.line2"))

      renderedEmail must not contain escapeMessage("mail.claim.next.send2")
      renderedEmail must not contain escapeMessage("mail.next.line")

      renderedEmail must not contain escapeMessage("mail.claim.next.line1.alt")
      renderedEmail must not contain escapeMessage("mail.claim.title")
      renderedEmail must not contain escapeMessage("mail.claim.successful")
      renderedEmail must not contain escapeMessage("mail.claim.next.line1")
    }

    "Display cofc email" in new WithApplication(){
      val claim = Claim(CachedClaim.key)
      implicit val lang = Lang("en")
      val renderedEmail = views.html.mail(claim,false,false).body

      renderedEmail.size must beGreaterThan(0)

      renderedEmail must contain(escapeMessage("mail.cofc.title"))
      renderedEmail must contain(escapeMessage("mail.cofc.successful"))
      renderedEmail must contain(escapeMessage("mail.next.line"))

      renderedEmail must not contain escapeMessage("mail.claim.title")
      renderedEmail must not contain escapeMessage("mail.claim.successful")
      renderedEmail must not contain escapeMessage("mail.claim.next.line1")
    }

  } section "unit"
}