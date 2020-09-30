package com.marka.state.db

import com.marka.state.domain.EntityState.EntityState
import zio.Task
import doobie.implicits._
import doobie._
import zio.interop.catz._
import cats.implicits._

case class TransitionIsNotAllowedException(from: EntityState, to: EntityState) extends Exception
case class TransitionAlreadyExists(from: EntityState, to: EntityState) extends Exception

trait TransitionMatrixService {
  def isAllowed(from: EntityState, to: EntityState): Task[Boolean]
  def allow(from: EntityState, to: EntityState): Task[Unit]
  def ban(from: EntityState, to: EntityState): Task[Unit]
}

class DefaultTransitionMatrixService(xa: Transactor[Task]) extends TransitionMatrixService {
  val queries = DefaultTransitionMatrixService

  override def isAllowed(from: EntityState, to: EntityState) =
    queries.get(from, to).transact(xa).map(_.isDefined)

  override def allow(from: EntityState, to: EntityState) =
    for {
      isAllowed <- isAllowed(from, to)
      _ <- if (!isAllowed) {
        queries.insert(from, to).transact(xa)
      } else {
        Task.fail(TransitionAlreadyExists(from, to))
      }
    } yield ()

  override def ban(from: EntityState, to: EntityState) =
    for {
      isAllowed <- isAllowed(from, to)
      _ <- if (!isAllowed) {
        queries.purge(from, to).transact(xa)
      } else {
        Task.fail(TransitionAlreadyExists(from, to))
      }
    } yield ()
}

object DefaultTransitionMatrixService extends DoobieSupport {
  def get(from: EntityState, to: EntityState) = {
    sql"""
          select `from`, `to` from transitions
          where `from` = $from and `to` = $to
       """.query[(EntityState, EntityState)].option
  }

  def insert(from: EntityState, to: EntityState) = {
    sql"""
          insert into transitions(`from`, `to`)
          values($from, $to)
       """.update.run.void
  }

  def purge(from: EntityState, to: EntityState) = {
    sql"""
          delete from transitions
          where `from` = $from and `to` = $to
       """.update.run.void
  }
}