package de.leanovate.play.cassandra.evolutions

import javax.inject.Inject

import play.api.db.evolutions._

class CassandraEvolutionsApi @Inject()(resolver: CassandraResolver) extends EvolutionsApi {
  override def scripts(db: String, evolutions: Seq[Evolution]): Seq[Script] =
    cassandraEvolutions(db).scripts(evolutions)

  override def resetScripts(db: String): Seq[Script] =
    cassandraEvolutions(db).resetScripts()

  override def evolve(db: String, scripts: Seq[Script], autocommit: Boolean) =
    cassandraEvolutions(db).evolve(scripts, autocommit)

  override def scripts(db: String, reader: EvolutionsReader): Seq[Script] =
    cassandraEvolutions(db).scripts(reader)

  override def resolve(db: String, revision: Int): Unit =
    cassandraEvolutions(db).resolve(revision)

  private def cassandraEvolutions(db: String) =
    new CassandraEvolutions(db, resolver.resolveCluster(db))
}
