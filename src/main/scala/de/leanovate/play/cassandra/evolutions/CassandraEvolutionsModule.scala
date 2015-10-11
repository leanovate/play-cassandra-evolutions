package de.leanovate.play.cassandra.evolutions

import play.api.db.evolutions.{EnvironmentEvolutionsReader, EvolutionsApi, EvolutionsReader}
import play.api.inject.Module
import play.api.{Configuration, Environment}

class CassandraEvolutionsModule extends Module {
  override def bindings(environment: Environment, configuration: Configuration) = {
    Seq(
      bind[EvolutionsReader].qualifiedWith("cassandra").to[CassandraEvolutionsReader],
      bind[EvolutionsApi].qualifiedWith("cassandra").to[CassandraEvolutionsApi],
      bind[ApplicationCassandraEvolutions].toSelf.eagerly()
    )
  }
}
