package com.anishathalye.oscar.reporter.email

import com.anishathalye.oscar.{ Result, Report, Success, Note, Failure }
import com.anishathalye.oscar.reporter.{ Reporter, ErrorReporter }

import org.apache.commons.mail.{ SimpleEmail, DefaultAuthenticator }

case class Email(
    hostname: String,
    port: Int,
    username: String,
    password: String) {

  def apply(emails: String*): Reporter = new Reporter {

    override def apply(name: String, result: Result) {
      var (overview, summary, description) = result match {
        case Success         => ("succeeded", "None", "None")
        case Note(report)    => (s"note at ${report.date}", report.summary, report.description getOrElse "None")
        case Failure(report) => (s"failed at ${report.date}", report.summary, report.description getOrElse "None")
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
