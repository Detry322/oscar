package com.anishathalye.oscar.checker.http

import com.anishathalye.oscar.Report
import com.anishathalye.oscar.{ Result, Success, Failure }
import com.anishathalye.oscar.checker.Checker
import com.anishathalye.oscar.Util.Pipe

import org.apache.commons.io.IOUtils
import org.apache.http._
import org.apache.http.auth._
import org.apache.http.client._
import org.apache.http.client.config._
import org.apache.http.client.methods._
import org.apache.http.entity._
import org.apache.http.impl.client._
import org.apache.http.protocol._

import java.util.Date
import java.io.IOException

case class HTTP(
  url: String,
  credentials: Option[CredentialsProvider] = None,
  contains: Option[String] = None,
  status: Int = 200,
  retries: Int = 3,
  timeout: Int = 5000, // milliseconds
  method: Method = Get,
  headers: Option[Map[String, String]] = None,
  allowRedirect: Boolean = false)
    extends Checker {

  def apply(): Result = {
    val client: HttpClient = HttpClientBuilder.create()
      .setRetryHandler(new DefaultHttpRequestRetryHandler(retries, true)) |> { cl =>
        if (!allowRedirect) {
          cl.setRedirectStrategy(new RedirectStrategy {
            override def getRedirect(request: HttpRequest, response: HttpResponse, context: HttpContext) = null
            override def isRedirected(request: HttpRequest, response: HttpResponse, context: HttpContext) = false
          })
        } else {
          cl
        }
      } |> { cl =>
        credentials match {
          case Some(cred) => cl.setDefaultCredentialsProvider(cred)
          case None       => cl
        }
      } |> { _.build() }
    val config = RequestConfig.custom()
      .setConnectTimeout(timeout)
      .setConnectionRequestTimeout(timeout)
      .setSocketTimeout(timeout)
      .build()
    def maybeSetEntity(entity: Option[HttpEntity])(req: HttpEntityEnclosingRequest with HttpRequestBase) = {
      entity foreach { ent =>
        req.setEntity(ent)
      }
      req
    }
    val request: HttpUriRequest with HttpRequestBase = method match {
      case Get         => new HttpGet(url)
      case Post(data)  => new HttpPost(url) |> maybeSetEntity(data)
      case Put(data)   => new HttpPut(url) |> maybeSetEntity(data)
      case Patch(data) => new HttpPatch(url) |> maybeSetEntity(data)
      case Delete      => new HttpDelete(url)
      case Head        => new HttpHead(url)
      case Options     => new HttpOptions(url)
      case Trace       => new HttpTrace(url)
    }
    request.setConfig(config)
    headers foreach { map =>
      map foreach {
        case (name, value) => request.setHeader(name, value)
      }
    }

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
        var handler = new BasicResponseHandler
        println(s"response => ${handler.handleResponse(response)}")
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

sealed trait Method
case object Get extends Method
case class Post(data: Option[HttpEntity] = None) extends Method {
  override def toString = "Post"
}
case class Put(data: Option[HttpEntity] = None) extends Method {
  override def toString = "Put"
}
case class Patch(data: Option[HttpEntity]) extends Method {
  override def toString = "Patch"
}
case object Delete extends Method
case object Head extends Method
case object Options extends Method
case object Trace extends Method

case object StringData {

  def apply(data: String): HttpEntity = new StringEntity(data)

}

object BasicAuthentication {

  def apply(username: String, password: String): CredentialsProvider = {
    val credentialsProvider = new BasicCredentialsProvider()
    credentialsProvider.setCredentials(AuthScope.ANY,
      new UsernamePasswordCredentials(username, password))
    credentialsProvider
  }

}
