package dao

import java.net.InetAddress

import com.datastax.driver.core.Cluster
import com.google.inject.Inject
import de.leanovate.play.cassandra.evolutions.CassandraEndpointConfig
import play.api.Configuration

import scala.collection.JavaConversions._

class EndpointsConfig @Inject()(
                                 configuration: Configuration
                                 ) extends CassandraEndpointConfig {
  override def databases: Seq[String] = Seq("cassandra")

  override def clusterForDatabase(db: String): Cluster = {
    val addresses: Seq[String] = configuration.getStringSeq("cassandra.hosts").getOrElse(Seq("localhost"))
    Cluster.builder()
      .addContactPoints(addresses.flatMap(InetAddress.getAllByName))
      .build()
  }
}
