package com.anishathalye.oscar.reporter

import com.anishathalye.oscar.{ Result, Failure }

object ConsoleReporter extends Reporter {

  override def apply(name: String, result: Result) {
    result match {
      case Failure(report) => print(s"[$name] @ ${report.date}  ${report.summary}")
      case _               => // do nothing
    }
  }

}
