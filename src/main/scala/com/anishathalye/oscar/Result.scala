package com.anishathalye.oscar

import java.util.Date

sealed abstract class Result

case class Success(report: Report) extends Result

case class Note(report: Report) extends Result

case class Failure(report: Report) extends Result

case class Report(date: Date, summary: Option[String], description: Option[String] = None)
