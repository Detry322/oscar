package com.anishathalye.oscar

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

}

object Util extends Util
