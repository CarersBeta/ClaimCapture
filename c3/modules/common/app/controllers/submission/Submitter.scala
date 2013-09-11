package controllers.submission

import scala.concurrent.Future
import play.api.mvc.{AnyContent, Request, PlainResult}
import models.domain.{DigitalForm, Claim}

trait Submitter {
  def submit(claim: DigitalForm, request : Request[AnyContent]): Future[PlainResult]
}
