package com.anishathalye.oscar.reporter

import com.anishathalye.oscar.{ Result, Success }

import concurrent.duration._

case class BackoffResetReporter(reporter: Reporter, low: Duration, high: Duration)
    extends BackoffReporter(reporter, low, high) {

  def resetTimeout(name: String) {
    this.synchronized {
      timeout -= name
    }
  }

  override def apply(name: String, result: Result) {
    // if successful, reset timeout
    result match {
      case Success(_) => {
        resetTimeout(name)
      }
      case _ => // do nothing
    }
  }

}
