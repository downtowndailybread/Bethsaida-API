package org.downtowndailybread.bethsaida.service

import java.util.UUID

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.directives.Credentials
import akka.http.scaladsl.server.directives.Credentials.{Missing, Provided}
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import org.downtowndailybread.bethsaida.exception.auth.{InvalidTokenSignatureException, UserNotAuthorizedException}
import org.downtowndailybread.bethsaida.model.{AnonymousUser, InternalUser}
import org.downtowndailybread.bethsaida.request.UserRequest
import org.downtowndailybread.bethsaida.request.util.DatabaseSource

import scala.concurrent.Future

trait AuthenticationProvider {

  this: UUIDProvider with SettingsProvider =>

  def authorize(isUserAuthorized: => InternalUser => Boolean): Directive1[InternalUser] = {
    extractRequestContext.flatMap {
      rc =>
        implicit val ec = rc.executionContext
        authenticateOAuth2Async(realm, c => Future {
          authenticateSignedToken(c)
        }).flatMap(iu =>
          onSuccess(Future {
            isUserAuthorized(iu)
          }).map {
            authorized =>
              if ((settings.allowAnonymousUser && iu == AnonymousUser) || authorized) {
                iu
              } else {
                throw new UserNotAuthorizedException
              }
          }
        )
    }
  }

  def authorizeNotAnonymous = authorize(u => u != AnonymousUser)

  def createSignedToken(userId: UUID): String = {
    JWT.create()
      .withIssuer(issuer)
      .withSubject(userId.toString)
      .sign(algorithm)
  }

  private[this] lazy val issuer = settings.provider
  private[this] lazy val algorithm = Algorithm.HMAC256(settings.secret)
  private[this] lazy val verifier = JWT.require(algorithm).withIssuer(issuer).build()

  private[this] val realm = settings.provider

  private def authenticateSignedToken(credentials: Credentials): Option[InternalUser] = {
    credentials match {
      case Missing => Some(AnonymousUser)
      case Provided(str) =>
        try {
          val decoded = verifier.verify(str)
          val id = decoded.getSubject
          DatabaseSource.runSql(conn => new UserRequest(conn).getRawUserFromUuid(parseUUID(id)))
        }
        catch {
          case e: JWTVerificationException => throw new InvalidTokenSignatureException
        }
    }
  }
}
