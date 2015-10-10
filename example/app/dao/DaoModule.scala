package dao

import de.leanovate.play.cassandra.evolutions.CassandraResolver
import play.api.inject.Module
import play.api.{Configuration, Environment}

class DaoModule extends Module {
  override def bindings(environment: Environment, configuration: Configuration) = {
    Seq(
      bind[CassandraResolver].to[DbResolver]
    )
  }
}
