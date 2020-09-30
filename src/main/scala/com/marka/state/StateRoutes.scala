package com.marka.state

import java.util.UUID

import com.marka.state.db.{EntityNotFoundException, EntityService}
import com.marka.state.domain.Encoders._
import com.marka.state.domain.{Entity, EntityState}
import com.marka.state.domain.EntityState.EntityState
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s.server.Router
import org.http4s.dsl.Http4sDsl
import zio.interop.catz._
import zio.interop.catz.implicits._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import zio.{Task, ZIO}

object StateRoutes {

  implicit val decoder = jsonOf[Task, PostStatePayload]

  def stateRoutes(entityService: EntityService): HttpRoutes[Task] = {
    val dsl = new Http4sDsl[Task]{}
    import dsl._

    HttpRoutes.of[Task] {
      case GET -> Root / "states" / id =>
        for {
          entity <- entityService.getEntity(UUID.fromString(id))
          resp <- Ok(entity)
        } yield resp

      case DELETE -> Root / "states" / id =>
        for {
          _ <- entityService.deleteEntity(UUID.fromString(id))
          resp <- Ok(id)
        } yield resp

      case req @ POST -> Root / "states" / id =>
        for {
          payload <- req.as[PostStatePayload]
          _ <- entityService.updateEntityState(UUID.fromString(id), EntityState.fromString(payload.state))
          resp <- Ok(id)
        } yield resp

      case PUT -> Root / "states" / name =>
        for {
          id <- entityService.addEntity(name)
          resp <- Ok(id)
        } yield resp
    }
  }
}

case class PostStatePayload(state: String)