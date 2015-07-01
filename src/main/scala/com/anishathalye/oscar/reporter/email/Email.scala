package com.anishathalye.oscar.reporter.email

import com.anishathalye.oscar.{ Result, Report, Success, Note, Failure }
import com.anishathalye.oscar.reporter.{ Reporter, ErrorReporter }
import com.anishathalye.oscar.Util.unwrap

import org.apache.commons.mail.{ SimpleEmail, DefaultAuthenticator }

case class Email(
    hostname: String,
    port: Int,
    username: String,
    password: String) {

  def apply(emails: String*): Reporter = new Reporter {

    override def apply(name: String, result: Result) {
      val (overview, summary, description) = result match {
        case Success(report) => (s"succeeded at ${report.date}", "None", "None")
        case Note(report)    => (s"note at ${report.date}", unwrap(report.summary), unwrap(report.description))
        case Failure(report) => (s"failed at ${report.date}", unwrap(report.summary), unwrap(report.description))
      }
      val email = new SimpleEmail()
      email setHostName hostname
      email setSmtpPort port
      email setAuthenticator new DefaultAuthenticator(username, password)
      email setTLS true
      email setFrom username
      email setSubject s"Oscar Report [$name]"
      email setMsg s"""Oscar check [$name] $overview
                      |
                      |Summary: $summary
                      |Details: $description""".stripMargin
      emails foreach { address =>
        email addTo address
      }
      email.send()
    }

  }

}
