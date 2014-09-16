package xml.claim

import models.domain.Claim
import xml.XMLHelper._
import xml.XMLComponent


object Consents extends XMLComponent {

  def xml(claim: Claim) = {
    val consent = claim.questionGroup[models.domain.Declaration].getOrElse(models.domain.Declaration())

    <Consents>
      {questionWhy(<Consent/>,"gettingInformationFromAnyEmployer.informationFromEmployer", consent.informationFromEmployer.answer, consent.informationFromEmployer.text, "gettingInformationFromAnyEmployer.why")}
      {questionWhy(<Consent/>,"tellUsWhyEmployer.informationFromPerson", consent.informationFromPerson.answer, consent.informationFromPerson.text, "tellUsWhyEmployer.whyPerson")}
    </Consents>
  }
}
