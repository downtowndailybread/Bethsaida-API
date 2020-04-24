package org.downtowndailybread.bethsaida.model

import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.server.PathMatcher.{Matched, Unmatched}
import akka.http.scaladsl.server.{PathMatcher, PathMatcher1}
import spray.json.{JsNumber, JsValue, RootJsonFormat}

import scala.util.Try

sealed abstract class ServiceType(val idx: Int, val string: String)


case object Shelter extends ServiceType(0, "shelter")
case object Shower extends ServiceType(1, "shower")



object ServiceTypeObj extends PathMatcher1[ServiceType] {

  private val all = List(Shelter, Shower)

  def apply(str: String): ServiceType = {
    val resp = all.find(_.string == str.toLowerCase).get
    resp
  }

  implicit val serviceTypeFormat = new RootJsonFormat[ServiceType] {
    override def read(json: JsValue): ServiceType = {
      json match {
        case JsNumber(num) =>
          val idx = num.toInt
          idx match {
            case 0 => Shelter
            case 1 => Shower
          }
      }
    }

    override def write(obj: ServiceType): JsValue = {
      JsNumber(obj.idx)
    }
  }

  override def apply(path: Uri.Path): PathMatcher.Matching[Tuple1[ServiceType]] = {
    path match {
      case Path.Segment(head, tail) => Try(Matched(tail, Tuple1(apply(head)))).toOption match {
        case Some(s) => s
        case None => Unmatched
      }
      case _ => Unmatched
    }
  }
}
