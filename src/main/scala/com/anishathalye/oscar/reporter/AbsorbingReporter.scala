package com.anishathalye.oscar.reporter

import com.anishathalye.oscar.{ Result, Report, Failure }

import collection.mutable.{ Map => MMap, HashMap }

object AbsorbingReporter {

  def apply(reporter: Reporter, capacity: Int = 1): Reporter = new Reporter {

    val absorbed: MMap[String, Int] = new HashMap[String, Int]()

    def getAbsorbed(name: String): Int = this.synchronized {
      absorbed lift name getOrElse 0
    }

    def setAbsorbed(name: String, count: Int) {
      this.synchronized {
        absorbed(name) = count
      }
    }

    override def apply(name: String, result: Result) {
      result match {
        case Failure(_) => {
          val soFar = getAbsorbed(name)
          if (soFar < capacity) {
            setAbsorbed(name, soFar + 1)
            // don't pass this on
          } else {
            // full
            reporter(name, result)
          }
        }
        case _ => {
          setAbsorbed(name, 0)
          reporter(name, result)
        }
      }
    }

  }

}
