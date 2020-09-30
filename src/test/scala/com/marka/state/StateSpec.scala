package com.marka.state

import java.util.UUID

import com.marka.state.db.EntityService
import com.marka.state.domain.EntityState.EntityState
import com.marka.state.domain.{Entity, EntityEvent, EntityState}
import org.http4s.implicits._
import org.http4s.{Method, Request, Response, Status}
import org.specs2.matcher.MatchResult
import zio.{Runtime, Task}
import zio.interop.catz.implicits._
import zio.interop.catz._
import zio.interop.catz.core._

class StateSpec extends org.specs2.mutable.Specification {

  "HelloWorld" >> {
    "return 200" >> {
      uriReturns200()
    }
  }

  private val ops = new EntityService {
    override def getEntity(id: UUID) =
      Task.succeed(Entity(id, "mark", EntityState.Init))

    override def addEntity(name: String) =
      Task.succeed(UUID.randomUUID)

    override def updateEntityState(id: UUID, to: EntityState) =
      Task.succeed(EntityEvent(UUID.randomUUID, UUID.randomUUID, EntityState.Init, to))

    override def deleteEntity(id: UUID) =
      Task.unit
  }

  private[this] val retHelloWorld: Response[Task] = {
    val getHW = Request[Task](Method.GET, uri"/states/123e4567-e89b-12d3-a456-426655440000")

    Runtime.default.unsafeRun {
      StateRoutes.stateRoutes(ops).run.apply(getHW).getOrElseF(Task.fail(new RuntimeException("mark lox")))
    }
  }

  private[this] def uriReturns200(): MatchResult[Status] =
    retHelloWorld.status must beEqualTo(Status.Ok)
}