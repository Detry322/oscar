package com.anishathalye.oscar

import java.util.Date

sealed abstract class Result

case object Success extends Result

case class Note(report: Report) extends Result

case class Failure(report: Report) extends Result

case class Report(date: Date, summary: String, description: Option[String])
