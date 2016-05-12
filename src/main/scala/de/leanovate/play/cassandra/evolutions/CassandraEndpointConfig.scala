package de.leanovate.play.cassandra.evolutions

import com.datastax.driver.core.{Cluster, Session}

/**
  * Implementors have to provide their own implementation of this trait (and create a binding in guice).
  *
  * This trait encapsulates all cassandra configurations.
  */
trait CassandraEndpointConfig {
  /**
    * Logical names of the databases that should be evolved.
    * This defines the subdirectory names in the 'conf/evolutions' directory and may or may not relate to
    * the keyspaces in the cassandra databases.
    *
    * @return Sequence of logical database names
    */
  def databases: Seq[String]

  /**
    * Get the cluster configuration of each logical database.
    *
    * @param db logical database name (from the sequence above)
    * @return Cassandra cluster configuration
    */
  def clusterForDatabase(db: String): Cluster

  /**
    * Get a session to a logical database.
    *
    * @param db logical database name (from the sequence above)
    * @return Cassandra cluster configuration
    */
  def sessionForDatabase(db: String): Session = clusterForDatabase(db).connect()

  /**
    * Run a block with a cluster wide lock.
    *
    * Execution should stop as long as necessary to ensure the only one `block` runs per cluster at a time.
    * This is used to prevent parallel evolutions of multiple nodes. If there is a different way for ensure this,
    * the default implementation will suffice (which does not lock at all).
    * Note: Cassandra itself provides no mechanism for table or schema locking.
    *
    * @param db    logical name of the database
    * @param block the block to be executed
    */
  def executeWithLock(db: String)(block: => Unit): Unit = {
    block
  }
}
