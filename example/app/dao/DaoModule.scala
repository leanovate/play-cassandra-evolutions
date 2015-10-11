package dao

import de.leanovate.play.cassandra.evolutions.CassandraEndpointConfig
import play.api.inject.Module
import play.api.{Configuration, Environment}

class DaoModule extends Module {
  override def bindings(environment: Environment, configuration: Configuration) = {
    Seq(
      bind[CassandraEndpointConfig].to[EndpointsConfig]
    )
  }
}
