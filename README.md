# play-cassandra-evolutions

Module for Play framework 2.4 to apply evolutions to a cassandra database. Behaves mostly like the standard evolution module for JDBC based databases

## Usage

The current version is supposed to run with Play 2.4 only.

Add the following dependency to your project

```
libraryDependencies += "de.leanovate" %% "play-cassandra-evolutions" % "1.0"
```

Since there are many ways to configure the contact points to a cassandra cluster, the module
does not make any presumptions about it. Instead you have to provide your own implementation for
the `de.leanovate.play.cassandra.evolutions.CassandraEndpointConfig` trait. The
most simple implementation might look like this

```
import com.datastax.driver.core.Cluster
import com.google.inject.Inject
import play.api.inject.Module
import play.api.{Configuration, Environment}

class LocalhostEndpointsConfig extends CassandraEndpointConfig {
  override def databases: Seq[String] = Seq("cassandra")

  override def clusterForDatabase(db: String): Cluster =
    Cluster.builder().addContactPoints("localhost").build()
}

class CassandraConfigModule extends Module {
  override def bindings(environment: Environment, configuration: Configuration) = {
    Seq(
      bind[CassandraEndpointConfig].to[LocalhostEndpointsConfig]
    )
  }
}
```

Which you have to enable in your application.conf

```
play.modules.enabled += CassandraConfigModule

```

As usually evolutions can now be added to (in the example above `dbname` is just 'cassandra':
* conf/evolutions/`dbname`/1.cql
* conf/evolutions/`dbname`/2.cql
* ...

Also as usual, automatic evolutions have to be activated in your configuration or via system property. E.g.:

```
play.evolutions.autoApply=true 
```

## License

[MIT Licence](http://opensource.org/licenses/MIT)
