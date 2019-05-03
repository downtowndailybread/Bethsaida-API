package org.downtowndailybread.bethsaida.service

import java.util.UUID

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive1, Route}
import akka.http.scaladsl.server.directives.Credentials
import akka.http.scaladsl.server.directives.Credentials.{Missing, Provided}
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import org.downtowndailybread.bethsaida.exception.auth.{InvalidTokenSignatureException, UserNotAuthorizedException}
import org.downtowndailybread.bethsaida.model.{AnonymousUser, InternalUser}
import org.downtowndailybread.bethsaida.request.{DatabaseSource, UserRequest}

trait AuthenticationProvider {

  this: UUIDProvider with SettingsProvider =>

  private[this] lazy val issuer = "bethsaida"
  private[this] lazy val algorithm = Algorithm.HMAC256(settings.secret)
  private[this] lazy val verifier = JWT.require(algorithm).withIssuer(issuer).build()

  val anonymousUser: InternalUser

  val realm = "bethsaida"

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

  def authorize(internalUser: InternalUser => Boolean): Directive1[InternalUser] = new Directive1[InternalUser] {
    override def tapply(f: Tuple1[InternalUser] => Route): Route =
      authenticateOAuth2(realm, authenticateSignedToken) {
        iu =>
          if ((settings.allowAnonymousUser && iu == AnonymousUser) || internalUser(iu)) {
            f.apply(Tuple1(iu))
          } else {
            throw new UserNotAuthorizedException()
          }
      }
  }

  def authorizeNotAnonymous = authorize(u => u != AnonymousUser)


  def createSignedToken(userId: UUID): String = {
    JWT.create()
      .withIssuer(issuer)
      .withSubject(userId.toString)
      .sign(algorithm)
  }

}
