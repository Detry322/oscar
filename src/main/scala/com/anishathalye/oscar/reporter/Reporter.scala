package com.anishathalye.oscar.reporter

import com.anishathalye.oscar.Result

trait Reporter {

  def apply(name: String, result: Result): Unit

}

object Reporter {

  def apply(func: (String, Result) => Unit): Reporter = new Reporter {

    override def apply(name: String, result: Result) {
      func(name, result)
    }

  }

}
