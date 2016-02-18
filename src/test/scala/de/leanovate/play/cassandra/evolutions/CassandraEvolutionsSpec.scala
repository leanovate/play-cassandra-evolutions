package de.leanovate.play.cassandra.evolutions

import java.util.Collections

import com.datastax.driver.core._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FlatSpec, MustMatchers}
import play.api.Environment

import org.mockito.Matchers._
import org.mockito.Mockito._
import play.api.db.evolutions.UpScript

class CassandraEvolutionsSpec extends FlatSpec with MockitoSugar with MustMatchers {

  it should "should read up scripts" in new WithMocks {
    val resultSet = mock[ResultSet]

    when(session.execute(any[Statement])).thenReturn(resultSet)
    when(resultSet.all()).thenReturn(Collections.emptyList[Row]())

    val scripts = evolutions.scripts(reader)

    scripts must have size 2
    scripts(0) mustBe an[UpScript]
    scripts(0).sql must equal("""CREATE KEYSPACE IF NOT EXISTS test WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1};""")
    scripts(1) mustBe an[UpScript]
    scripts(1).sql must equal("""CREATE TABLE IF NOT EXISTS test.table1 (
                                |id varchar primary key
                                |);""".stripMargin)
  }

  trait WithMocks {
    val reader = new CassandraEvolutionsReader(Environment.simple())
    val cluster = mock[Cluster]
    val session = mock[Session]
    val clusterConfiguration = mock[Configuration]
    val protocalOptions = mock[ProtocolOptions]

    when(cluster.connect()).thenReturn(session)
    when(cluster.getConfiguration).thenReturn(clusterConfiguration)
    when(clusterConfiguration.getProtocolOptions).thenReturn(protocalOptions)

    val evolutions = new CassandraEvolutions("fixture", cluster)
  }
}
