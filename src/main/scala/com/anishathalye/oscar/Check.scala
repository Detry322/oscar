package com.anishathalye.oscar

import com.anishathalye.oscar.checker.Checker
import com.anishathalye.oscar.reporter.Reporter

import concurrent.duration.Duration

case class Check(
  name: String,
  frequency: Duration,
  checker: Checker,
  reporter: Reporter)
    extends Schedulable {

  override def run() {
    reporter(name, checker())
  }

}
