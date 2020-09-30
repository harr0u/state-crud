package com.marka.state.db

import java.util.UUID

import com.marka.state.domain.{Entity, EntityEvent, EntityState}
import doobie.implicits._
import doobie.util.transactor.Transactor
import zio._
import zio.interop.catz._
import cats.implicits._
import com.marka.state.{Configuration, DBOps}
import com.marka.state.domain.EntityState.EntityState
import zio.blocking.Blocking


case class EntityNotFoundException(id: UUID) extends Exception

trait EntityService {
  def getEntity(id: UUID): Task[Entity]
  def addEntity(name: String): Task[UUID]
  def updateEntityState(id: UUID, to: EntityState): Task[EntityEvent]
  def deleteEntity(id: UUID): Task[Unit]
}


class DefaultEntityService(transitionMatrix: TransitionMatrixService, xa: Transactor[Task]) extends EntityService {
  val queries = DefaultEntityService

  override def getEntity(id: UUID): Task[Entity] = {
    queries
      .get(id)
      .transact(xa)
      .flatMap(_.fold[Task[Entity]](Task.fail(EntityNotFoundException(id)))(Task.succeed(_)))
  }

  override def addEntity(name: String): Task[UUID] = {
    queries.insert(name).transact(xa)
  }

  override def updateEntityState(id: UUID, to: EntityState): Task[EntityEvent] = {
    for {
      entity <- getEntity(id)
      allowed <- transitionMatrix.isAllowed(entity.state, to)
      eventId <- if (allowed) {
        (queries.updateState(id, to) *>
          queries.insertEvent(id, entity.state, to)
          ).transact(xa)
      } else {
        Task.fail(TransitionIsNotAllowedException(entity.state, to))
      }
    } yield EntityEvent(eventId, entity.id, entity.state, to)
  }

  override def deleteEntity(id: UUID) = {
    for {
      _ <- getEntity(id)
      _ <- queries.purgeEntity(id).transact(xa)
    } yield ()
  }
}

object DefaultEntityService extends DoobieSupport {
  def get(id: UUID): doobie.ConnectionIO[Option[Entity]] = {
    sql"""
          select id, name, state from entities
          where id = $id
       """.query[Entity].option
  }

  def insert(name: String) = {
    val defaultState = EntityState.Init
    val id = UUID.randomUUID
    sql"""
          insert into entities(id, name, state)
          values ($id, $name, $defaultState)
       """.update.run.map(_ => id)
  }

  def updateState(id: UUID, state: EntityState) = {
    sql"""
          update entities
          set state = $state
          where id = $id
       """.update.run.void
  }

  def insertEvent(entityId: UUID, from: EntityState, to: EntityState) = {
    val id = UUID.randomUUID
    sql"""
          insert into entity_events(id, entity_id, `from`, `to`)
          values ($id, $entityId, $from, $to)
       """.update.run.map(_ => id)
  }

  def purgeEntity(id: UUID) = {
    sql"""
          delete from entity_events
          where id = $id
       """.update.run.void
  }
}
