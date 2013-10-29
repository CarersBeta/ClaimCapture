package xml

import app.XMLValues
import scala.language.reflectiveCalls
import scala.xml.{NodeSeq, Elem}
import models.domain._
import xml.XMLHelper._
import app.XMLValues._

object Employment {

  def employerXml(job: Job): Elem = {
    val jobDetails = job.questionGroup[JobDetails].getOrElse(JobDetails())
    val employerContactDetails = job.questionGroup[EmployerContactDetails].getOrElse(EmployerContactDetails())

    <Employer>
      <DateJobStarted>
        <QuestionLabel>job.started</QuestionLabel>
        {<Answer/> +++ Some(jobDetails.jobStartDate.`dd-MM-yyyy`)}
      </DateJobStarted>

      {if(!jobDetails.lastWorkDate.isEmpty){
        <DateJobEnded>
          <QuestionLabel>job.ended</QuestionLabel>
          {<Answer/> +++ jobDetails.lastWorkDate}
        </DateJobEnded>
      }}
      {job.title.isEmpty match {
        case false => {
          <JobType>
            <QuestionLabel>job.title</QuestionLabel>
            <Answer>{job.title}</Answer>
          </JobType>
        }
        case true => NodeSeq.Empty
      }}
      {<ClockPayrollNumber/> +++ jobDetails.payrollEmployeeNumber}
      <Name>{jobDetails.employerName}</Name>
      <Address>{postalAddressStructure(employerContactDetails.address, employerContactDetails.postcode)}</Address>
      {<EmployersPhoneNumber/> +++ employerContactDetails.phoneNumber}
    </Employer>
  }

  def payXml(jobDetails: JobDetails, lastWage: LastWage, additionalWageDetails: AdditionalWageDetails): Elem = {
    <Pay>
      <WeeklyHoursWorked>
        <QuestionLabel>job.hours</QuestionLabel>
        {<Answer/> ?+ jobDetails.hoursPerWeek}
      </WeeklyHoursWorked>

      {if(!lastWage.lastPaidDate.isEmpty){
        <DateLastPaid>
          <QuestionLabel>job.lastpaid</QuestionLabel>
          {<Answer/> +++ lastWage.lastPaidDate}
        </DateLastPaid>
      }}

      <GrossPayment>
        <QuestionLabel>job.pay</QuestionLabel>
        <Answer>
          <Currency>{GBP}</Currency>
          {<Amount/> +++ lastWage.grossPay}
        </Answer>
      </GrossPayment>
      <IncludedInWage>
        <QuestionLabel>job.pay.include</QuestionLabel>
        {<Answer/> +++ lastWage.payInclusions}
      </IncludedInWage>
      {paymentFrequency(additionalWageDetails.oftenGetPaid)}
      <UsualPayDay>
        <QuestionLabel>job.day</QuestionLabel>
        {<Answer/>+- additionalWageDetails.whenGetPaid}
      </UsualPayDay>
    </Pay>
  }

  def howMuchOwedXml(s: Option[String]) = {
    val showXml = s.isDefined

    if (showXml) {
      <Payment>
        <Currency>{GBP}</Currency>
        <Amount>{s.get}</Amount>
      </Payment>
    } else {
      NodeSeq.Empty
    }
  }

  def pensionSchemeXml(job: Job) = {
    val pensionScheme:PensionSchemes = job.questionGroup[PensionSchemes].getOrElse(PensionSchemes())

    NodeSeq.Empty ++ {occupationalPensionSchemeXml(pensionScheme)} ++ {personalPensionSchemeXml(pensionScheme)}
  }

  def occupationalPensionSchemeXml(pensionScheme: PensionSchemes) = {
    val showXml = pensionScheme.payOccupationalPensionScheme == yes

    if (showXml) {
      <PaidForOccupationalPension>
        <QuestionLabel>pension.occupational</QuestionLabel>
        <Answer>{pensionScheme.payOccupationalPensionScheme  match {
            case "yes" => XMLValues.Yes
            case "no" => XMLValues.No
            case n => n
        }}</Answer>
      </PaidForOccupationalPension>

        <OccupationalPension>
          <Payment>
            <QuestionLabel>pension.occ.amount</QuestionLabel>
            <Answer>
              <Currency>{GBP}</Currency>
              {<Amount/> +++ pensionScheme.howMuchPension}
            </Answer>
          </Payment>
          <Frequency>
            <QuestionLabel>pension.occ.frequency</QuestionLabel>
            <Answer>{pensionScheme.howOftenPension.get.frequency}</Answer>
          </Frequency>
        </OccupationalPension>
    } else {
      NodeSeq.Empty
    }
  }

  def personalPensionSchemeXml(pensionScheme:PensionSchemes): NodeSeq = {
    val showXml = pensionScheme.payPersonalPensionScheme == yes

    if (showXml) {
      <PaidForPersonalPension>
        <QuestionLabel>pension.personal</QuestionLabel>
        <Answer>{pensionScheme.payPersonalPensionScheme  match {
          case "yes" => XMLValues.Yes
          case "no" => XMLValues.No
          case n => n
        }}</Answer>
      </PaidForPersonalPension>
        <PersonalPension>
          <Payment>
            <QuestionLabel>pension.per.amount</QuestionLabel>
            <Answer>
              <Currency>{GBP}</Currency>
              {<Amount/> +++ pensionScheme.howMuchPersonal}
            </Answer>
          </Payment>
          <Frequency>
            <QuestionLabel>pension.per.frequency</QuestionLabel>
            <Answer>{pensionScheme.howOftenPersonal.get.frequency}</Answer>
          </Frequency>
        </PersonalPension>
    } else {
      NodeSeq.Empty
    }
  }

  def jobExpensesXml(job: Job) = {
    val aboutExpenses: AboutExpenses = job.questionGroup[AboutExpenses].getOrElse(AboutExpenses())
    val necessaryExpenses: NecessaryExpenses = job.questionGroup[NecessaryExpenses].getOrElse(NecessaryExpenses())
    val showXml = aboutExpenses.payForAnythingNecessary == "yes"

    if (showXml) {
        <PaidForJobExpenses>
          <QuestionLabel>job.expenses</QuestionLabel>
          <Answer>{aboutExpenses.payForAnythingNecessary match {
            case "yes" => XMLValues.Yes
            case "no" => XMLValues.No
            case n => n
          }}</Answer>
        </PaidForJobExpenses>
      <JobExpenses>
        <Expense>
          <QuestionLabel>job.expense</QuestionLabel>
          <Answer>{necessaryExpenses.whatAreThose}</Answer>
        </Expense>
      </JobExpenses>
    } else {
      <PaidForJobExpenses>{aboutExpenses.payForAnythingNecessary}</PaidForJobExpenses>
    }
  }

  def childcareExpensesXml(job: Job) = {
    val aboutExpenses: AboutExpenses = job.questionGroup[AboutExpenses].getOrElse(AboutExpenses())
    val childcareExpenses: ChildcareExpenses = job.questionGroup[ChildcareExpenses].getOrElse(ChildcareExpenses())
    val showXml = aboutExpenses.payAnyoneToLookAfterChildren == yes

    if (showXml) {
        <CareExpensesChildren>
          <QuestionLabel>chld.expenses</QuestionLabel>
          <Answer>{aboutExpenses.payAnyoneToLookAfterChildren match {
            case "yes" => XMLValues.Yes
            case "no" => XMLValues.No
            case n => n
          }}</Answer>
        </CareExpensesChildren>

      <ChildCareExpenses>
        <CarerName>
          <QuestionLabel>child.carer</QuestionLabel>
          <Answer>{childcareExpenses.whoLooksAfterChildren}</Answer>
        </CarerName>
        <RelationshipCarerToClaimant>
          <QuestionLabel>child.care.rel.claimant</QuestionLabel>
          <Answer>{childcareExpenses.relationToYou}</Answer>
        </RelationshipCarerToClaimant>
      </ChildCareExpenses>
    } else {
      <CareExpensesChildren>{aboutExpenses.payAnyoneToLookAfterChildren}</CareExpensesChildren>
    }
  }

  def careExpensesXml(job: Job) = {
    val aboutExpenses: AboutExpenses = job.questionGroup[AboutExpenses].getOrElse(AboutExpenses())
    val personYouCareExpenses: PersonYouCareForExpenses = job.questionGroup[PersonYouCareForExpenses].getOrElse(PersonYouCareForExpenses())

    val showXml = aboutExpenses.payAnyoneToLookAfterPerson == yes

    if (showXml) {
      <CareExpensesCaree>
        <QuestionLabel>care.expenses</QuestionLabel>
        <Answer>{aboutExpenses.payAnyoneToLookAfterPerson match {
          case "yes" => XMLValues.Yes
          case "no" => XMLValues.No
          case n => n
        }}</Answer>
      </CareExpensesCaree>
      <CareExpenses>
        <CarerName>
          <QuestionLabel>child.carer</QuestionLabel>
          <Answer>{personYouCareExpenses.whoDoYouPay}</Answer>
        </CarerName>
        <RelationshipCarerToClaimant>
          <QuestionLabel>child.care.rel.claimant</QuestionLabel>
          <Answer>{personYouCareExpenses.relationToYou}</Answer>
        </RelationshipCarerToClaimant>
        <RelationshipCarerToCaree>
          <QuestionLabel>care.carer.rel.caree</QuestionLabel>
          <Answer>{personYouCareExpenses.relationToPersonYouCare}</Answer>
        </RelationshipCarerToCaree>
      </CareExpenses>
    } else {
      <CareExpensesCaree>{aboutExpenses.payAnyoneToLookAfterPerson}</CareExpensesCaree>
    }
  }

  def xml(claim: Claim) = {
    val jobsQG = claim.questionGroup[Jobs].getOrElse(Jobs())
    val employment = claim.questionGroup[models.domain.Employment]

    if (jobsQG.jobs.length > 0 && employment.fold(false)(_.beenEmployedSince6MonthsBeforeClaim == "yes")) {
      // We will search in all jobs for at least one case finishedThisJob = "no" because that means he is currently employed
      val currentlyEmployed = jobsQG.jobs.count(_.apply(JobDetails) match {
        case Some(j: JobDetails) => j.finishedThisJob == no
        case _ => false
      }) match {
        case i if i > 0 => yes
        case _ => no
      }

      <Employment>
        <CurrentlyEmployed>
          <QuestionLabel>employed.currently</QuestionLabel>
          <Answer>{currentlyEmployed match {
            case "yes" => XMLValues.Yes
            case "no" => XMLValues.No
            case n => n
          }}</Answer>
        </CurrentlyEmployed>
        {for (job <- jobsQG) yield {
          val jobDetails = job.questionGroup[JobDetails].getOrElse(JobDetails())
          val lastWage = job.questionGroup[LastWage].getOrElse(LastWage())
          val additionalWageDetails = job.questionGroup[AdditionalWageDetails].getOrElse(AdditionalWageDetails())

          <JobDetails>
            {employerXml(job)}
            {payXml(jobDetails, lastWage, additionalWageDetails)}
            <OweMoney>
              <QuestionLabel>job.owe</QuestionLabel>
              <Answer>{additionalWageDetails.employerOwesYouMoney match {
                case "yes" => XMLValues.Yes
                case "no" => XMLValues.No
                case n => n
              }}</Answer>
            </OweMoney>
            {childcareExpensesXml(job)}
            {careExpensesXml(job)}
            {pensionSchemeXml(job)}
            {jobExpensesXml(job)}
          </JobDetails>
        }}
      </Employment>
    } else {
      NodeSeq.Empty
    }
  }
}