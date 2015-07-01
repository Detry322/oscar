package com.anishathalye.oscar.checker

import com.anishathalye.oscar.Result

trait Checker {

  def apply(): Result

}

object Checker {

  def apply(func: () => Result): Checker = new Checker {

    override def apply(): Result = func()

  }

}
