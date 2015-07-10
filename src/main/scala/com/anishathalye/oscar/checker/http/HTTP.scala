package com.anishathalye.oscar.checker.http

import com.anishathalye.oscar.Report
import com.anishathalye.oscar.{ Result, Success, Failure }
import com.anishathalye.oscar.checker.Checker

import org.apache.commons.io.IOUtils
import org.apache.http._
import org.apache.http.client._
import org.apache.http.client.config._
import org.apache.http.client.methods._
import org.apache.http.impl.client._
import org.apache.http.protocol._

import java.util.Date
import java.io.IOException

case class HTTP(
  url: String,
  contains: Option[String] = None,
  status: Int = 200,
  retries: Int = 3,
  timeout: Int = 5000, // milliseconds
  method: Method = GET)
    extends Checker {

  def apply(): Result = {
    val client: HttpClient = HttpClientBuilder.create()
      .setRetryHandler(new DefaultHttpRequestRetryHandler(retries, true))
      .setRedirectStrategy(new RedirectStrategy {
        override def getRedirect(request: HttpRequest, response: HttpResponse, context: HttpContext) = null
        override def isRedirected(request: HttpRequest, response: HttpResponse, context: HttpContext) = false
      })
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
        contains match {
          case None => Success(Report(new Date(), None, None))
          case Some(check) => {
            val contentStream = response.getEntity.getContent
            val encoding = Option(response.getEntity.getContentEncoding) map {
              _.getValue
            } getOrElse "UTF-8"
            val body = IOUtils.toString(contentStream, if (encoding != null) encoding else encoding)
            if (body contains check) {
              Success(Report(new Date(), None, None))
            } else {
              Failure(Report(new Date(), Some(s"tried $method $url, missing content $check")))
            }
          }
        }
      } else {
        return Failure(Report(new Date(), Some(s"tried $method $url, got $code expecting $status")))
      }
    } catch {
      case e @ (_: ClientProtocolException | _: IOException) => {
        return Failure(Report(new Date(), Some(s"tried $method $url expecting $status, exception thrown"),
          Some(e.toString())
        ))
      }
    }
  }

}

sealed abstract class Method
case object GET extends Method
