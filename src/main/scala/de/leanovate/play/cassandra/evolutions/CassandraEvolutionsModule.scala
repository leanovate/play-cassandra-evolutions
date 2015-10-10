package de.leanovate.play.cassandra.evolutions

import play.api.db.evolutions.{EnvironmentEvolutionsReader, EvolutionsReader, EvolutionsApi}
import play.api.inject.Module
import play.api.{Configuration, Environment}

class CassandraEvolutionsModule extends Module {
  override def bindings(environment: Environment, configuration: Configuration) = {
    Seq(
      bind[EvolutionsReader].to[EnvironmentEvolutionsReader],
      bind[EvolutionsApi].to[CassandraEvolutionsApi]
    )
  }
}
