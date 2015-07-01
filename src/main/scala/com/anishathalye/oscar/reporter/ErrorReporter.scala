package com.anishathalye.oscar.reporter

import com.anishathalye.oscar.{ Result, Report, Failure }

object ErrorReporter {

  def apply(func: (String, Report) => Unit): Reporter = new Reporter {

    override def apply(name: String, result: Result) {
      result match {
        case Failure(report) => func(name, report)
        case _               => // do nothing
      }
    }

  }

}
