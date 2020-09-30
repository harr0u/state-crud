package com.marka.state

import java.util.UUID

import com.marka.state.domain.Entity
import com.marka.state.domain.EntityState.EntityState
import io.circe.{Decoder, Encoder, Json}
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf
import zio.Task

package object domain {
  final case class Entity(id: UUID, name: String, state: EntityState)

  final case class EntityEvent(id: UUID, entityId: UUID, from: EntityState, to: EntityState)


  object EntityState extends Enumeration {
    def fromString(from: String) = from.toLowerCase match {
      case "init" => EntityState.Init
      case "pending" => EntityState.Pending
      case "finished" => EntityState.Finished
      case "closed" => EntityState.Closed
      case _ => EntityState.Unknown
    }

    def toString(v: EntityState) = {
      v match {
        case Init => "Init"
        case Pending => "Pending"
        case Finished => "Finished"
        case Closed => "Closed"
        case Unknown => "Unknown"
      }
    }

    type EntityState = Value
    val Init, Pending, Finished, Closed, Unknown = Value
  }

  object Encoders {
    implicit val EntityEncoder: Encoder[Entity] = (a: Entity) => Json.obj(
      ("id", Json.fromString(a.id.toString)),
      ("state", Json.fromString(EntityState.toString(a.state)))
    )
    implicit def EntityEntityEncoder: EntityEncoder[Task, Entity] =
      jsonEncoderOf[Task, Entity]

    implicit val BooleanEncoder: Encoder[Boolean] = Json.fromBoolean

    implicit def BooleanEntityEncoder: EntityEncoder[Task, Boolean] =
      jsonEncoderOf[Task, Boolean]

    implicit val PostStatePayloadEncoder: Encoder[PostStatePayload] = (a: PostStatePayload) => Json.obj(
      ("state", Json.fromString(a.state.toString))
    )

    implicit def PostStatePayloadEntityEncoder: EntityEncoder[Task, PostStatePayload] =
      jsonEncoderOf[Task, PostStatePayload]

    implicit val UUIDEncoder: Encoder[UUID] = (a: UUID) => Json.fromString(a.toString)

    implicit def UUIDEntityEncoder: EntityEncoder[Task, UUID] =
      jsonEncoderOf[Task, UUID]

  }
}

