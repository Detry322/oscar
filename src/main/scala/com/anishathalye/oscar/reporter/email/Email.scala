package com.anishathalye.oscar.reporter.email

import com.anishathalye.oscar.Result
import com.anishathalye.oscar.reporter.{ Reporter, ErrorReporter }

import org.apache.commons.mail.{ SimpleEmail, DefaultAuthenticator }

case class Email(
    hostname: String,
    port: Int,
    username: String,
    password: String) {

  def apply(emails: String*): Reporter = ErrorReporter({ (name, report) =>
    val email = new SimpleEmail()
    email setHostName hostname
    email setSmtpPort port
    email setAuthenticator new DefaultAuthenticator(username, password)
    email setTLS true
    email setFrom username
    email setSubject s"Oscar Report [$name]"
    email setMsg s"""Oscar check [$name] failed at ${report.date}
                    |
                    |Summary: ${report.summary}
                    |Details: ${report.description getOrElse "None"}""".stripMargin
    emails foreach { address =>
      email addTo address
    }
    email.send()
  })

}
