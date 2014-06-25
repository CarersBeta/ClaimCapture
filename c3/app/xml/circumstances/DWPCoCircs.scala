package xml.circumstances

import scala.xml.Elem
import play.api.Logger
import models.domain.Claim

object DWPCoCircs {

  def xml(circs: Claim): Elem = {
    Logger.info(s"Build DWPCoCircs")

    <DWPCAChangeOfCircumstances>
      {Claimant.xml(circs)}
      {Caree.xml(circs)}
      {CircsBreaksInCare.xml(circs)}
      {StoppedCaring.xml(circs)}
      {AddressChange.xml(circs)}
      {PaymentChange.xml(circs)}
      {SelfEmployment.xml(circs)}
      {OtherChanges.xml(circs)}
      {EmploymentChange.xml(circs)}
      {Declaration.xml(circs)}
      {Consents.xml(circs)}
    </DWPCAChangeOfCircumstances>
  }

}
