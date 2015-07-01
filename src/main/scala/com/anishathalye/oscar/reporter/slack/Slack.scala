package com.anishathalye.oscar.reporter.slack

import com.anishathalye.oscar.{ Result, Report, Success, Note, Failure }
import com.anishathalye.oscar.reporter.{ Reporter, ErrorReporter }

import spray.json._
import DefaultJsonProtocol._

import org.apache.http._
import org.apache.http.client._
import org.apache.http.client.config._
import org.apache.http.client.methods._
import org.apache.http.entity._
import org.apache.http.impl.client._
import org.apache.http.params._

case class Slack(
  url: String,
  username: String = "oscar",
  emoji: Option[String] = None,
  channel: Option[String] = None)
    extends Reporter {

  override def apply(name: String, result: Result) {
    var summary = result match {
      case Success         => "succeeded"
      case Note(report)    => s"note: ${report.summary}${report.description map (", " + _) getOrElse ""}"
      case Failure(report) => s"failed: ${report.summary}${report.description map (", " + _) getOrElse ""}"
    }
    val client = HttpClients.createDefault()
    val post = new HttpPost(url)
    val message = Map(
      "username" -> username,
      "text" -> s"[$name] $summary"
    )
    val withChannel = channel map { c => message + ("channel" -> c) } getOrElse message
    val withEmoji = emoji map { e => withChannel + ("icon_emoji" -> e) } getOrElse withChannel
    val stringEntity = new StringEntity(withEmoji.toJson.compactPrint)
    post setEntity stringEntity
    post.setHeader("Content-type", "application/json")
    client execute post
  }

}
