package xml

import org.specs2.mutable.{Tags, Specification}
import app.XMLValues._
import play.api.test.WithApplication

class XmlHelperSpec extends Specification with Tags {

  "XMLHelper" should {

    "convert boolean string to yes/no" in {
      "when true" in {
        XMLHelper.booleanToYesNo(true) shouldEqual Yes
      }

      "when false" in {
        XMLHelper.booleanToYesNo(false) shouldEqual No
      }
    }

    "construct a basic question" in {
      "when question has an answer of type text." in new WithApplication {
        XMLHelper.question(<Test/>,"s7.g1", "Yes").toString shouldEqual "<Test><QuestionLabel>Have you been employed?</QuestionLabel><Answer>Yes</Answer></Test>"
      }
      "when question has an answer of type boolean." in new WithApplication {
        XMLHelper.question(<Test/>,"s7.g1", true).toString shouldEqual "<Test><QuestionLabel>Have you been employed?</QuestionLabel><Answer>Yes</Answer></Test>"
      }
      "when question has an answer of type nodeSeq." in new WithApplication {
        XMLHelper.question(<Test/>,"s7.g1", <myNode>hello</myNode>).toString shouldEqual "<Test><QuestionLabel>Have you been employed?</QuestionLabel><Answer><myNode>hello</myNode></Answer></Test>"
      }

      "when question with question label having two arguments." in new WithApplication {
        XMLHelper.question(<Test/>,"beenEmployedSince6MonthsBeforeClaim", <myNode>hello</myNode>,"arg1","arg2").toString shouldEqual "<Test><QuestionLabel>Have you had another job at any time since arg1 (this is six months before your claim date: arg2)?</QuestionLabel><Answer><myNode>hello</myNode></Answer></Test>"
      }

    }
  }

  "construct an other question" in {
    "when question has other option" in new WithApplication{
      XMLHelper.questionOther(<Test/>,"s7.g1", "Other", Some("Maybe")).toString shouldEqual "<Test><QuestionLabel>Have you been employed?</QuestionLabel><Other>Maybe</Other><Answer>Other</Answer></Test>"
    }
    "when question has why option" in new WithApplication{
      XMLHelper.questionWhy(<Test/>,"s7.g1", "No", Some("Maybe")).toString shouldEqual "<Test><QuestionLabel>Have you been employed?</QuestionLabel><Answer>No</Answer><Why>Maybe</Why></Test>"
    }

    "when question is about currency" in new WithApplication{
      XMLHelper.questionCurrency(<Test/>,"s7.g1", Some("122.12")).toString shouldEqual "<Test><QuestionLabel>Have you been employed?</QuestionLabel><Answer><Currency>GBP</Currency><Amount>122.12</Amount></Answer></Test>"
    }
  }
}