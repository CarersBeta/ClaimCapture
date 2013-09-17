package models.domain

import scala.reflect.ClassTag
import models.{Timestamped, DayMonthYear}
import models.view.Navigation
import scala.xml.Elem
import com.dwp.carers.s2.xml.validation.XmlValidator
import play.Configuration
import scala.concurrent.ExecutionContext
import play.api.Play
import play.api.Play.current

/**
 * Represents the data gathered from customers through the views.
 * The data are divided into sections, themselves sub-divided into question groups.
 */
abstract class DigitalForm(val sections: List[Section] = List())(implicit val navigation: Navigation = Navigation()) extends Timestamped {
  def copyForm(sections: List[Section])(implicit navigation: Navigation):DigitalForm

  def xmlValidator:XmlValidator

  def xml(transactionId: String):Elem

  def cacheKey:String

  def dateOfClaim: Option[DayMonthYear]

  def section(sectionIdentifier: Section.Identifier): Section = sections.find(s => s.identifier == sectionIdentifier) match {
    case Some(s: Section) => s
    case _ => Section(sectionIdentifier, List())
  }

  def previousSection(sectionIdentifier: Section.Identifier): Section = {
    sections.filter(s => s.identifier.index < sectionIdentifier.index && s.visible).lastOption match {
      case Some(s: Section) => s
      case _ => section(sectionIdentifier)
    }
  }

  def previousSection(questionGroupIdentifier: QuestionGroup.Identifier): Section = previousSection(Section.sectionIdentifier(questionGroupIdentifier))

  def questionGroup(questionGroupIdentifier: QuestionGroup.Identifier): Option[QuestionGroup] = {
    val si = Section.sectionIdentifier(questionGroupIdentifier)
    section(si).questionGroup(questionGroupIdentifier)
  }

  def questionGroup[Q <: QuestionGroup](implicit classTag: ClassTag[Q]): Option[Q] = {
    def needQ(qg: QuestionGroup): Boolean = {
      qg.getClass == classTag.runtimeClass
    }

    sections.flatMap(_.questionGroups).find(needQ) match {
      case Some(q: Q) => Some(q)
      case _ => None
    }
  }

  def previousQuestionGroup(questionGroupIdentifier: QuestionGroup.Identifier): Option[QuestionGroup] = completedQuestionGroups(questionGroupIdentifier).lastOption

  def completedQuestionGroups(sectionIdentifier: Section.Identifier): List[QuestionGroup] = section(sectionIdentifier).questionGroups

  def completedQuestionGroups(questionGroupIdentifier: QuestionGroup.Identifier): List[QuestionGroup] = {
    val si = Section.sectionIdentifier(questionGroupIdentifier)
    section(si).precedingQuestionGroups(questionGroupIdentifier)
  }

  def update(section: Section) = {
    val updatedSections = sections.takeWhile(_.identifier.index < section.identifier.index) :::
      List(section) :::
      sections.dropWhile(_.identifier.index <= section.identifier.index)

    copyForm(sections = updatedSections.sortWith(_.identifier.index < _.identifier.index))
  }

  def +(section: Section) = update(section)

  def update(questionGroup: QuestionGroup): DigitalForm = {
    val si = Section.sectionIdentifier(questionGroup.identifier)
    update(section(si).update(questionGroup))
  }

  def +(questionGroup: QuestionGroup): DigitalForm = update(questionGroup)

  def delete(questionGroupIdentifier: QuestionGroup.Identifier): DigitalForm = {
    val sectionIdentifier = Section.sectionIdentifier(questionGroupIdentifier)
    update(section(sectionIdentifier).delete(questionGroupIdentifier))
  }

  def -(questionGroupIdentifier: QuestionGroup.Identifier): DigitalForm = delete(questionGroupIdentifier)

  def hideSection(sectionIdentifier: Section.Identifier): DigitalForm = update(section(sectionIdentifier).hide)

  def showSection(sectionIdentifier: Section.Identifier): DigitalForm = update(section(sectionIdentifier).show)

  def showHideSection(visible: Boolean, sectionIdentifier: Section.Identifier) = {
    if (visible) showSection(sectionIdentifier) else hideSection(sectionIdentifier)
  }

  def isBot(questionGroup: QuestionGroup): Boolean = {
    DigitalForm.checkForBot match {
      case true => isFormFilledFasterThanAHumanCanType(questionGroup, System.currentTimeMillis(), created)
      case false => false
    }
  }

  def isFormFilledFasterThanAHumanCanType(questionGroup: QuestionGroup, currentTime: Long, formCreatedTime: Long): Boolean = {
    val worldRecordWordsPerMinute = 212 // http://en.wikipedia.org/wiki/Words_per_minute
    val conversionFactorToCharactersPerWord = 5
    val millisecondsPerCharacter: Double = (60000: Double) / (worldRecordWordsPerMinute * conversionFactorToCharactersPerWord) //(25940: Double) / (160: Double) // http://blog.gsmarena.com/world-record-in-fast-texting-broken-160-characters-typed-in-25-94-seconds-on-a-galaxy-s/
    val minimumMillisToInputThisNumOfChars: Double = questionGroup.numberOfCharactersInput * millisecondsPerCharacter
    val millisBetweenPresentAndSubmit = currentTime - formCreatedTime

    millisBetweenPresentAndSubmit < minimumMillisToInputThisNumOfChars
  }
}

object DigitalForm{
  val checkForBot: Boolean = play.Configuration.root().getBoolean("checkForBot", false) //Play.configuration.getBoolean("checkForBot").getOrElse(true) //Play.current.configuration.getBoolean("checkForBot").getOrElse(true) //play.Configuration.root().getBoolean("checkForBot", true)
}