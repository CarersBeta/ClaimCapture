package models.domain

import language.postfixOps
import models.{DayMonthYear, Timestamped}
import scala.reflect.ClassTag
import models.view.Navigation
import scala.xml.Elem
import xml.DWPCAClaim

case class Claim(sections: List[Section] = List())(implicit navigation: Navigation = Navigation()) extends DigitalForm(sections)(navigation) {
//  def section(sectionIdentifier: Section.Identifier): Section = sections.find(s => s.identifier == sectionIdentifier) match {
//    case Some(s: Section) => s
//    case _ => Section(sectionIdentifier, List())
//  }
//
//  def previousSection(sectionIdentifier: Section.Identifier): Section = {
//    sections.filter(s => s.identifier.index < sectionIdentifier.index && s.visible).lastOption match {
//      case Some(s: Section) => s
//      case _ => section(sectionIdentifier)
//    }
//  }
//
//  def previousSection(questionGroupIdentifier: QuestionGroup.Identifier): Section = previousSection(Section.sectionIdentifier(questionGroupIdentifier))
//
//  def questionGroup(questionGroupIdentifier: QuestionGroup.Identifier): Option[QuestionGroup] = {
//    val si = Section.sectionIdentifier(questionGroupIdentifier)
//    section(si).questionGroup(questionGroupIdentifier)
//  }
//
//  def questionGroup[Q <: QuestionGroup](implicit classTag: ClassTag[Q]): Option[Q] = {
//    def needQ(qg: QuestionGroup): Boolean = {
//      qg.getClass == classTag.runtimeClass
//    }
//
//    sections.flatMap(_.questionGroups).find(needQ) match {
//      case Some(q: Q) => Some(q)
//      case _ => None
//    }
//  }
//
//  def previousQuestionGroup(questionGroupIdentifier: QuestionGroup.Identifier): Option[QuestionGroup] = completedQuestionGroups(questionGroupIdentifier).lastOption
//
//  def completedQuestionGroups(sectionIdentifier: Section.Identifier): List[QuestionGroup] = section(sectionIdentifier).questionGroups
//
//  def completedQuestionGroups(questionGroupIdentifier: QuestionGroup.Identifier): List[QuestionGroup] = {
//    val si = Section.sectionIdentifier(questionGroupIdentifier)
//    section(si).precedingQuestionGroups(questionGroupIdentifier)
//  }
//
//  def update(section: Section): Claim = {
//    val updatedSections = sections.takeWhile(_.identifier.index < section.identifier.index) :::
//                          List(section) :::
//                          sections.dropWhile(_.identifier.index <= section.identifier.index)
//
//    copy(sections = updatedSections.sortWith(_.identifier.index < _.identifier.index))
//  }
//
//  def +(section: Section): Claim = update(section)
//
//  def update(questionGroup: QuestionGroup): Claim = {
//    val si = Section.sectionIdentifier(questionGroup.identifier)
//    update(section(si).update(questionGroup))
//  }
//
//  def +(questionGroup: QuestionGroup): Claim = update(questionGroup)
//
//  def delete(questionGroupIdentifier: QuestionGroup.Identifier): Claim = {
//    val sectionIdentifier = Section.sectionIdentifier(questionGroupIdentifier)
//    update(section(sectionIdentifier).delete(questionGroupIdentifier))
//  }
//
//  def -(questionGroupIdentifier: QuestionGroup.Identifier): Claim = delete(questionGroupIdentifier)
//
//  def dateOfClaim: Option[DayMonthYear] = questionGroup(ClaimDate) match {
//    case Some(c: ClaimDate) => Some(c.dateOfClaim)
//    case _ => None
//  }
//
//  def hideSection(sectionIdentifier: Section.Identifier): Claim = update(section(sectionIdentifier).hide)
//
//  def showSection(sectionIdentifier: Section.Identifier): Claim = update(section(sectionIdentifier).show)
//
//  def showHideSection(visible: Boolean, sectionIdentifier: Section.Identifier) = {
//    if (visible) showSection(sectionIdentifier) else hideSection(sectionIdentifier)
//  }
//  def copy(sections: List[Section])(implicit navigation: Navigation): DigitalForm = this.copy()
  def copyForm(sections: List[Section]): DigitalForm = this.copy(sections)

  def xml(transactionId:String): Elem = DWPCAClaim.xml(this,transactionId)
}