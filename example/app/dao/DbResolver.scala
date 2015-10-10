package dao

import com.datastax.driver.core.Cluster
import de.leanovate.play.cassandra.evolutions.CassandraResolver

class DbResolver extends CassandraResolver {
  override def resolveCluster(db: String): Cluster = {
    Cluster.builder().addContactPoints("localhost").build()
  }
}
