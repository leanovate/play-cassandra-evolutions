package dao

import com.datastax.driver.core.Cluster
import de.leanovate.play.cassandra.evolutions.CassandraEndpointConfig

class EndpointsConfig extends CassandraEndpointConfig {
  override def databases: Seq[String] = Seq("cassandra")

  override def clusterForDatabase(db: String): Cluster = {
    Cluster.builder().addContactPoints("10.211.55.19").build()
  }
}
