package com.anishathalye.oscar

import concurrent.duration.Duration

trait Schedulable extends Runnable {

  def frequency: Duration

  override def run(): Unit

}
