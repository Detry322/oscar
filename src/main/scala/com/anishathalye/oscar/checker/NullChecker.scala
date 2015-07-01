package com.anishathalye.oscar.checker

import com.anishathalye.oscar.{ Result, Success, Report }

import java.util.Date

object NullChecker extends Checker {

  override def apply(): Result = Success(Report(new Date(), None, None))

}
