package com.anishathalye.oscar.reporter.sms

import com.anishathalye.oscar.Result
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

  def apply(phones: String*): Reporter = ErrorReporter({ (name, report) =>
    val client = new TwilioRestClient(account, token)
    val factory = client.getAccount.getMessageFactory
    val message = s"Oscar[$name] ${report.summary}"

    phones foreach { phone =>
      val params: JList[NameValuePair] = new ArrayList[NameValuePair]()
      params add new BasicNameValuePair("Body", message)
      params add new BasicNameValuePair("From", from)
      params add new BasicNameValuePair("To", phone)
      factory create params
    }
  })

}
