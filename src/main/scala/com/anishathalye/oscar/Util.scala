package com.anishathalye.oscar

import java.util.{ Date, TimeZone }
import java.text.SimpleDateFormat

trait Transformation[T] {

  def apply(x: T): T

}

object Transformation {

  def apply[T](func: T => T): Transformation[T] = new Transformation[T] {

    override def apply(x: T): T = func(x)

  }

}

trait Util {

  def unwrap(message: Option[String]): String = message getOrElse "None"

  def dateToIsoString(date: Date): String = {
    val tz = TimeZone getTimeZone "UTC"
    val df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    df setTimeZone tz
    df format date
  }

}

object Util extends Util
