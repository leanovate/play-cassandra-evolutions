package de.leanovate.play.cassandra.evolutions

import com.datastax.driver.core.Cluster

trait CassandraEndpointConfig {
  def databases: Seq[String]

  def clusterForDatabase(db: String): Cluster
}
