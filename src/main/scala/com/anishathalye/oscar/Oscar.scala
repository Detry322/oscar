package com.anishathalye.oscar

import collection.mutable.{ HashMap, HashSet, Map => MMap, Set => MSet, PriorityQueue }
import concurrent.duration._
import math._

import java.util.Date

class Oscar(schedulables: List[Schedulable]) {
  require(schedulables.size > 0)

  val MIN_SLEEP_DURATION = 250.milliseconds

  val threads: MMap[Schedulable, Thread] = new HashMap[Schedulable, Thread]()

  val lastLaunched: MMap[Schedulable, Date] = new HashMap[Schedulable, Date]()

  case class Task(time: Date, task: Schedulable)

  // flip ordering so it's a min queue
  implicit def taskOrdering: Ordering[Task] = Ordering.fromLessThan(_.time after _.time)

  // store time to run next
  val queue: PriorityQueue[Task] = new PriorityQueue[Task]()

  val scheduled: MSet[Schedulable] = new HashSet[Schedulable]()

  def running(task: Schedulable): Boolean = {
    threads lift task map { _.isAlive } getOrElse false
  }

  def scheduleTasks() {
    val now = new Date()
    schedulables foreach { schedulable =>
      if (!(scheduled contains schedulable)) {
        val base = lastLaunched lift schedulable getOrElse now
        val time = new Date(base.getTime + schedulable.frequency.toMillis)
        queue += Task(time, schedulable)
        scheduled += schedulable
      }
    }
  }

  def launchTask(task: Schedulable, time: Date) {
    scheduled -= task
    lastLaunched(task) = time
    val thread = new Thread(task)
    thread.start()
    threads(task) = thread
  }

  def launchTasks() {
    val now = new Date()
    val waiting = new HashSet[Task]()
    while (queue.nonEmpty) {
      val next = queue.dequeue()
      if (next.time before now) {
        if (!running(next.task)) {
          launchTask(next.task, now)
        } else {
          waiting += next
        }
      } else {
        queue += next
        if (waiting.size > 0) {
          queue ++= waiting
        }
        return // launched everything we can for now
      }
    }
  }

  def run() {
    while (true) {
      launchTasks()
      scheduleTasks()
      assert(queue.size == schedulables.size)
      // everything should be present exactly once in the queue now
      val nextLaunch = (new Date()).getTime - queue.head.time.getTime
      val sleepTime = max(nextLaunch, MIN_SLEEP_DURATION.toMillis)
      try {
        Thread sleep sleepTime
      } catch {
        case _: InterruptedException => // do nothing
      }
    }
  }

}

object Oscar {

  def apply(schedulables: List[Schedulable]) = new Oscar(schedulables)

}
