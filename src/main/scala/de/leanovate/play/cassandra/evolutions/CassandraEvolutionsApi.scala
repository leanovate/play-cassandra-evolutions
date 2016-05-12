package de.leanovate.play.cassandra.evolutions

import javax.inject.Inject

import play.api.db.evolutions._

class CassandraEvolutionsApi @Inject()(endpointsConfig: CassandraEndpointConfig) extends EvolutionsApi {
  override def scripts(db: String, evolutions: Seq[Evolution], schema: String): Seq[Script] =
    cassandraEvolutions(db).scripts(evolutions)

  override def resetScripts(db: String, schema: String): Seq[Script] =
    cassandraEvolutions(db).resetScripts()

  override def evolve(db: String, scripts: Seq[Script], autocommit: Boolean, schema: String) =
    cassandraEvolutions(db).evolve(scripts, autocommit)

  override def scripts(db: String, reader: EvolutionsReader, schema: String): Seq[Script] =
    cassandraEvolutions(db).scripts(reader)

  override def resolve(db: String, revision: Int, schema: String): Unit =
    cassandraEvolutions(db).resolve(revision)

  private def cassandraEvolutions(db: String) =
    new CassandraEvolutions(db, endpointsConfig)
}
