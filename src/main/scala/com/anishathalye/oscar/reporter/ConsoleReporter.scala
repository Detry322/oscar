package com.anishathalye.oscar.reporter

import com.anishathalye.oscar.{ Result, Failure }

import java.util.Date

object ConsoleReporter extends Reporter {

  override def apply(name: String, result: Result) {
    println(s"[$name] $result (${new Date()})")
  }

}
