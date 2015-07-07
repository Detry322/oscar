package com.anishathalye.oscar.reporter

import com.anishathalye.oscar.{ Result, Success, Failure }

import collection.mutable.{ Map => MMap, HashMap }

case class HealReporter(
  reporter: Reporter,
  initialState: Boolean = false)
    extends Reporter {

  // whether last check was successful
  val last: MMap[String, Boolean] = new HashMap[String, Boolean]()

  def getLast(name: String): Boolean = this.synchronized {
    last lift name getOrElse initialState
  }

  def setLast(name: String, successful: Boolean) {
    this.synchronized {
      last(name) = successful
    }
  }

  override def apply(name: String, result: Result) {
    result match {
      case Success(report) => {
        if (getLast(name) == false) {
          // switched state to healed
          reporter(name, result)
          setLast(name, true)
        }
      }
      case Failure(report) => {
        setLast(name, false)
      }
      case _ => // do nothing
    }
  }

}
