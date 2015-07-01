package com.anishathalye.oscar.checker.http

import com.anishathalye.oscar.Report
import com.anishathalye.oscar.{ Result, Success, Failure }
import com.anishathalye.oscar.checker.Checker

import org.apache.http._
import org.apache.http.client._
import org.apache.http.client.methods._
import org.apache.http.impl.client._

import java.util.Date
import java.io.IOException

case class HTTP(
  url: String,
  status: Int,
  retries: Int = 3,
  method: Method = GET)
    extends Checker {

  val client: HttpClient = HttpClientBuilder.create()
    .setRetryHandler(new DefaultHttpRequestRetryHandler(retries, true))
    .build()

  def apply(): Result = {
    val method = new HttpGet(url)

    try {
      val response = client execute method
      val code = response.getStatusLine.getStatusCode
      if (code == status) {
        return Success
      } else {
        return Failure(Report(new Date(), s"tried $method $url, got $code expecting $status"))
      }
    } catch {
      case e @ (_: ClientProtocolException | _: IOException) => // do nothing, return failure later
    }
    return Failure(Report(new Date(), s"tried $method $url expecting $status, exception thrown"))
  }

}

sealed abstract class Method
case object GET extends Method
