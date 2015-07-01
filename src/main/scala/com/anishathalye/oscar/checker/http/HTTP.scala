package com.anishathalye.oscar.checker.http

import com.anishathalye.oscar.Report
import com.anishathalye.oscar.{ Result, Success, Failure }
import com.anishathalye.oscar.checker.Checker

import org.apache.http._
import org.apache.http.params._
import org.apache.http.client._
import org.apache.http.client.config._
import org.apache.http.client.methods._
import org.apache.http.impl.client._

import java.util.Date
import java.io.IOException

case class HTTP(
  url: String,
  status: Int = 200,
  retries: Int = 3,
  timeout: Int = 5000, // milliseconds
  method: Method = GET)
    extends Checker {

  def apply(): Result = {
    val client: HttpClient = HttpClientBuilder.create()
      .setRetryHandler(new DefaultHttpRequestRetryHandler(retries, true))
      .build()
    val config = RequestConfig.custom()
      .setConnectTimeout(timeout)
      .setConnectionRequestTimeout(timeout)
      .setSocketTimeout(timeout)
      .build()
    val request = new HttpGet(url)

    try {
      val response = client execute request
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
