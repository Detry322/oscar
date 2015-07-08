package com.anishathalye.oscar.reporter.web

import com.anishathalye.oscar.{ Result, Report, Success, Note, Failure }
import com.anishathalye.oscar.reporter.{ Reporter, ErrorReporter }
import com.anishathalye.oscar.Util

import spray.json._
import DefaultJsonProtocol._

import org.http4s.dsl._
import org.http4s.server.HttpService
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.middleware.CORS

import java.util.Date

import collection.mutable.{ Map => MMap, HashMap }

case class Web(port: Int) extends Reporter {

  val statuses: MMap[String, Result] = new HashMap[String, Result]()

  val lastFailed: MMap[String, Date] = new HashMap[String, Date]()

  def serialize(name: String, result: Result): Map[String, String] = {
    val (status, report) = result match {
      case Success(r) => ("success", r)
      case Note(r)    => ("note", r)
      case Failure(r) => ("failure", r)
    }
    Map(
      "name" -> name,
      "status" -> status,
      "date" -> Util.dateToIsoString(report.date),
      "last_failed" -> (lastFailed lift name map {
        Util.dateToIsoString(_)
      } getOrElse ""),
      "summary" -> (report.summary getOrElse ""),
      "description" -> (report.description getOrElse "")
    )
  }
  val service = CORS(HttpService {
    case GET -> Root / "status" => this.synchronized {
      val serialized = statuses.toList map {
        case (name, report) => serialize(name, report)
      }
      Ok(serialized.toJson.prettyPrint)
    }
    case GET -> Root / "status" / name => this.synchronized {
      if (name.isEmpty) {
        // get all
        val serialized = statuses.toList map {
          case (name, report) => serialize(name, report)
        }
        Ok(serialized.toJson.prettyPrint)
      } else {
        // get a specific resource
        statuses lift name match {
          case Some(result) => Ok(serialize(name, result).toJson.prettyPrint)
          case None         => NotFound("not found")
        }
      }
    }
  })

  val thread = new Thread(new Runnable {
    override def run() {
      BlazeBuilder.bindHttp(port, "0.0.0.0")
        .mountService(service)
        .run
        .awaitShutdown()
    }
  })
  thread.start()

  override def apply(name: String, result: Result) {
    this.synchronized {
      statuses(name) = result
      result match {
        case Failure(report) => lastFailed(name) = report.date
        case _               => // ignore
      }
    }
  }

}
