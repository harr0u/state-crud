package com.marka.state

import java.util.UUID

import com.marka.state.db.{DefaultEntityService, DefaultTransitionMatrixService, EntityService}
import org.http4s.implicits._
import cats.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import zio.interop.catz.implicits._
import org.http4s.server.Router
import zio._
import cats.effect.{Blocker, ConcurrentEffect}
import com.marka.state.domain.EntityState.EntityState
import com.marka.state.domain.{Entity, EntityEvent, EntityState}
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor
import zio.interop.catz._

import scala.concurrent.ExecutionContext

object Main extends zio.App {
  val server = Task.concurrentEffectWith { implicit ce => {
    for {
      res <- BlazeServerBuilder.apply[Task](ExecutionContext.global)
        .bindHttp(8080, "localhost")
        .withHttpApp(Router(
          "/api" -> (
            StateRoutes.stateRoutes(ServerOps.entityService) <+>
            TransitionRoutes.transitionRoutes(ServerOps.transitionService))
        ).orNotFound)
        .serve
        .compile
        .drain
    } yield res
  }}

  def run(args: List[String]) =
    server.fold(_ => zio.ExitCode.failure, _ => zio.ExitCode.success)
}

object DBOps {
  private val config = new HikariConfig()
  config.setDriverClassName("com.mysql.cj.jdbc.Driver")
  config.setJdbcUrl("jdbc:mysql://localhost:3306/state?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC")
  config.setUsername("root")
  config.setPassword("rootroot")
  config.setMaximumPoolSize(16)

  val transactor: Transactor[Task] = Transactor.fromDataSource[Task](
    new HikariDataSource(config),
    ExecutionContext.global,
    Blocker.liftExecutionContext(ExecutionContext.global)
  )
}

object ServerOps {
  val transitionService = new DefaultTransitionMatrixService(DBOps.transactor)
  val entityService = new DefaultEntityService(transitionService, DBOps.transactor)
}

case class Configuration()
