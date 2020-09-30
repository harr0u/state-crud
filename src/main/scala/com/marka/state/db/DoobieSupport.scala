package com.marka.state.db

import java.util.UUID

import com.marka.state.domain.EntityState
import com.marka.state.domain.EntityState.EntityState
import doobie.Meta

trait DoobieSupport {
  implicit val `String <-> UUID`: Meta[UUID] = Meta[String].timap(UUID.fromString)(_.toString)

  implicit val `Int <-> EntityState`: Meta[EntityState] =
    Meta[Int].timap {
      case 0 => EntityState.Init
      case 1 => EntityState.Pending
      case 2 => EntityState.Finished
      case 3 => EntityState.Closed
      case _ => EntityState.Unknown
    } {
      case EntityState.Init => 0
      case EntityState.Pending => 1
      case EntityState.Finished => 2
      case EntityState.Closed => 3
      case EntityState.Unknown => 4
    }
}
