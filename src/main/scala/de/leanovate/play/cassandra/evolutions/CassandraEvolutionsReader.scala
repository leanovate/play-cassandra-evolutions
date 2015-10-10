package de.leanovate.play.cassandra.evolutions

import java.io.{FileInputStream, InputStream}
import javax.inject.Inject

import play.api.Environment
import play.api.db.evolutions.ResourceEvolutionsReader

class CassandraEvolutionsReader @Inject()(environment: Environment) extends ResourceEvolutionsReader {
  override def loadResource(db: String, revision: Int): Option[InputStream] = {
    environment.getExistingFile(CassandraEvolutions.fileName(db, revision)).map(new FileInputStream(_)).orElse {
      environment.resourceAsStream(CassandraEvolutions.resourceName(db, revision))
    }
  }
}
