package models.view

import play.api.mvc.{Action, AnyContent, Request, Result}
import play.api.cache.Cache
import models.domain.Claim
import play.Configuration
import play.api.Play._
import play.api.mvc.Results.Redirect

trait CachedClaim {
  import play.api.Play
  import play.api.Logger
  import scala.language.implicitConversions
  import play.api.http.HeaderNames._
  import java.util.UUID._

  implicit def defaultResultToLeft(result: Result) = Left(result)

  implicit def claimAndResultToRight(claimingResult: (Claim, Result)) = Right(claimingResult)

  def newClaim(f: => Claim => Request[AnyContent] => Result) = Action {
    implicit request => {
      val (key, expiration) = keyAndExpiration(request)

      def apply(claim: Claim) = f(claim)(request).withSession("connected" -> key)
        .withHeaders(CACHE_CONTROL -> "no-cache, no-store")
        .withHeaders("X-Frame-Options" -> "SAMEORIGIN") // stop click jacking

      if (request.getQueryString("changing").getOrElse("false") == "false") {
        val claim = Claim()
        Cache.set(key, claim, expiration)
        Logger.info("Starting new claim")
        apply(claim)
      } else {
        Cache.getAs[Claim](key) match {
          case Some(claim) => apply(claim)
          case None => Redirect("/timeout")
        }
      }
    }
  }

  def claiming(f: => Claim => Request[AnyContent] => Either[Result, (Claim, Result)]) = Action {
    request => {
      val (key, expiration) = keyAndExpiration(request)

      def action(claim: Claim): Result = {
        f(claim)(request) match {
          case Left(r: Result) =>
            r.withSession("connected" -> key)
              .withHeaders(CACHE_CONTROL -> "no-cache, no-store")
              .withHeaders("X-Frame-Options" -> "SAMEORIGIN") // stop click jacking

          case Right((c: Claim, r: Result)) => {
            Cache.set(key, c, expiration)

            r.withSession("connected" -> key)
              .withHeaders(CACHE_CONTROL -> "no-cache, no-store")
              .withHeaders("X-Frame-Options" -> "SAMEORIGIN") // stop click jacking
          }
        }
      }

      Cache.getAs[Claim](key) match {
        case Some(claim) => action(claim)

        case None =>
          if (Play.isTest) {
            val claim = Claim()
            Cache.set(key, claim, 20) // place an empty claim in the cache to satisfy tests
            action(claim)
          } else {
            Logger.info("Claim timeout")
            Redirect("/timeout")
          }
      }
    }
  }

  def claimingInJob(f: => Claim => Request[AnyContent] => Either[Result, (Claim, Result)]) = Action {
    request => {
      claiming(f)(request).flashing("jobID"->request.body.asFormUrlEncoded.getOrElse(Map("jobID"->Seq("")))("jobID").applyOrElse(0,(i:Int)=>""))
    }
  }

  private def keyAndExpiration(r: Request[AnyContent]): (String, Int) = {
    r.session.get("connected").getOrElse(randomUUID.toString) -> Configuration.root().getInt("cache.expiry", 3600)
  }
}