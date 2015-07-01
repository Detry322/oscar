package com.anishathalye.oscar.reporter

import com.anishathalye.oscar.Result

object NullReporter extends Reporter {

  override def apply(name: String, result: Result) {
    // do nothing
  }

}
