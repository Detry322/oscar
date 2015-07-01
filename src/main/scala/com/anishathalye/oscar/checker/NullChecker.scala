package com.anishathalye.oscar.checker

import com.anishathalye.oscar.{ Result, Success }

object NullChecker extends Checker {

  override def apply(): Result = Success

}
