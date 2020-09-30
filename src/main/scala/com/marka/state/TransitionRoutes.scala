package com.marka.state

import java.util.UUID

import com.marka.state.db.{EntityService, TransitionMatrixService}
import com.marka.state.domain.Encoders._
import com.marka.state.domain.EntityState
import org.http4s.HttpRoutes
import org.http4s.server.Router
import org.http4s.dsl.Http4sDsl
import zio.interop.catz._
import zio.interop.catz.implicits._
import zio.{Task, ZIO}

object TransitionRoutes {

  def transitionRoutes(transitionMatrixService: TransitionMatrixService): HttpRoutes[Task] = {
    val dsl = new Http4sDsl[Task]{}
    import dsl._
    HttpRoutes.of[Task] {
      case GET -> Root / "transitions" / from / to =>
        for {
          isAllowed <- transitionMatrixService.isAllowed(EntityState.fromString(from), EntityState.fromString(to))
          resp <- Ok(isAllowed)
        } yield resp
      case DELETE -> Root / "transitions" / from / to =>
        for {
          _ <- transitionMatrixService.ban(EntityState.fromString(from), EntityState.fromString(to))
          resp <- Ok(())
        } yield resp
      case PUT -> Root / "transitions" / from / to =>
        for {
          _ <- transitionMatrixService.allow(EntityState.fromString(from), EntityState.fromString(to))
          resp <- Ok(())
        } yield resp
    }
  }
}