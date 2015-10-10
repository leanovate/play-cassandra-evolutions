package de.leanovate.play.cassandra.evolutions

import com.datastax.driver.core.Cluster

trait CassandraResolver {
  def resolveCluster(db: String): Cluster
}
