package com.anishathalye.oscar.reporter.sms

import com.anishathalye.oscar.{ Result, Report, Success, Note, Failure }
import com.anishathalye.oscar.reporter.{ Reporter, ErrorReporter }

import com.twilio.sdk.TwilioRestClient
import com.twilio.sdk.TwilioRestException
import com.twilio.sdk.resource.factory.MessageFactory
import com.twilio.sdk.resource.instance.Message
import org.apache.http.NameValuePair
import org.apache.http.message.BasicNameValuePair

import java.util.{ List => JList, ArrayList }

case class SMS(
    account: String,
    token: String,
    from: String) {

  def apply(phones: String*): Reporter = new Reporter {

    override def apply(name: String, result: Result) {
      val summary = result match {
        case Success(_)      => "succeeded"
        case Note(report)    => s"note: ${report.summary}"
        case Failure(report) => s"failed: ${report.summary}"
      }
      val client = new TwilioRestClient(account, token)
      val factory = client.getAccount.getMessageFactory
      val message = s"Oscar[$name] $summary"

      phones foreach { phone =>
        val params: JList[NameValuePair] = new ArrayList[NameValuePair]()
        params add new BasicNameValuePair("Body", message)
        params add new BasicNameValuePair("From", from)
        params add new BasicNameValuePair("To", phone)
        factory create params
      }
    }

  }

}
