package com.anishathalye.oscar

import com.anishathalye.oscar.checker.{ Checker, NullChecker }
import com.anishathalye.oscar.reporter.{ Reporter, NullReporter }

import language.implicitConversions
import concurrent.duration._

trait DSL {

  val DEFAULT_FREQUENCY: Duration = 1.minute

  object NullTransformation {

    implicit val transformation: Transformation[Check] = Transformation(identity)

  }

  def check(name: String)(implicit transformation: Transformation[Check]): Check = {
    transformation(Check(name, DEFAULT_FREQUENCY, NullChecker, NullReporter))
  }

  implicit class CheckOps(check: Check) {

    def every(frequency: Duration): Check = check.copy(frequency = frequency)

    def by(checker: Checker): Check = check.copy(checker = checker)

    def report(reporter: Reporter): Check = check.copy(reporter = reporter)

  }

  implicit class ReporterOps(reporter: Reporter) {

    def ~>(next: Reporter): Reporter = Reporter({ (name, result) =>
      reporter(name, result)
      next(name, result)
    })

  }

  implicit def rawToOption[T](raw: T): Option[T] = Option(raw)

}

object DSL extends DSL
