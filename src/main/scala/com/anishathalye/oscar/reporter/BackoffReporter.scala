package com.anishathalye.oscar.reporter

import com.anishathalye.oscar.Result

import concurrent.duration._
import collection.mutable.{ Map => MMap, HashMap }
import math._

import java.util.Date

// low and high in seconds
case class BackoffReporter(reporter: Reporter, low: Duration, high: Duration) extends Reporter {

  val FACTOR = 2

  val last: MMap[String, Date] = new HashMap[String, Date]()

  def getLast(name: String): Option[Date] = this.synchronized {
    last lift name
  }

  def setLast(name: String, time: Date) {
    this.synchronized {
      last(name) = time
    }
  }

  val timeout: MMap[String, Duration] = new HashMap[String, Duration]()

  def getTimeout(name: String): Option[Duration] = this.synchronized {
    timeout lift name
  }

  def setTimeout(name: String, time: Duration) {
    this.synchronized {
      timeout(name) = time
    }
  }

  override def apply(name: String, result: Result) {
    val now = new Date()
    getLast(name) flatMap { last =>
      getTimeout(name) map { timeout =>
        (last, timeout)
      }
    } match {
      case None => {
        setLast(name, now)
        setTimeout(name, low)
        reporter(name, result)
      }
      case Some((last, timeout)) => {
        // check if we need to do exponential backoff
        val diff = now.getTime - last.getTime
        val threshold = timeout.toMillis
        if (diff < threshold) {
          // suppress, hasn't been long enough
        } else if (diff < threshold * FACTOR) {
          // send it, double threshold
          setLast(name, now)
          setTimeout(name, min(threshold * FACTOR, high.toMillis).milliseconds)
          reporter(name, result)
        } else {
          // send it, shrink threshold
          setLast(name, now)
          setTimeout(name, max(threshold / FACTOR, low.toMillis).milliseconds)
          reporter(name, result)
        }
      }
    }
  }

}
