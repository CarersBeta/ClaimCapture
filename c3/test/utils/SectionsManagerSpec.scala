package utils

import models.view.CachedClaim
import org.specs2.mutable._
import models.domain._
import models.domain.Claim

class SectionsManagerSpec extends Specification {
  section("unit")
  "Sections Manager" should {
    "Retrieve the correct number of sections" in {
      SectionsManager.claimSectionsNum(Claim(CachedClaim.key)) mustEqual 11
    }

    "Retrieve the correct section position for some Sections" in {
      implicit val claim = Claim(CachedClaim.key)
      SectionsManager.currentSection(AboutYou) mustEqual 2
      SectionsManager.currentSection(Employed) mustEqual 8
    }

    "Retrieve the correct section positions when there are hidden sections" in new WithApplication {
      implicit val claim = Claim(CachedClaim.key).showHideSection(visible = false,PayDetails)

      SectionsManager.currentSection(AboutYou) mustEqual 2
      SectionsManager.currentSection(OtherMoney) mustEqual 9
    }

    "Retrieve the correct section positions when there are hidden employment" in new WithApplication {
      implicit val claim = Claim(CachedClaim.key).showHideSection(visible = false,SelfEmployment)

      SectionsManager.currentSection(Employed) mustEqual 7
      SectionsManager.currentSection(PayDetails) mustEqual 9
    }
  }
  section("unit")
}
