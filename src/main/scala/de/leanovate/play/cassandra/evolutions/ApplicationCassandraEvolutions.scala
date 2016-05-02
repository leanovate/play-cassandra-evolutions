package de.leanovate.play.cassandra.evolutions

import javax.inject.{Inject, Named, Singleton}

import play.api.db.evolutions._
import play.api.{Environment, Logger, Mode}
import play.core.WebCommands

@Singleton
class ApplicationCassandraEvolutions @Inject()(
                                                config: EvolutionsConfig,
                                                endpointsConfig: CassandraEndpointConfig,
                                                @Named("cassandra") reader: EvolutionsReader,
                                                @Named("cassandra") evolutions: EvolutionsApi,
                                                environment: Environment,
                                                webCommands: WebCommands
                                                ) {
  private val logger = Logger(classOf[ApplicationCassandraEvolutions])

  def start(): Unit = {
    webCommands.addHandler(new CassandraEvolutionsWebCommands(evolutions, reader, config))

    endpointsConfig.databases.foreach(runEvolutions)
  }

  private def runEvolutions(db: String): Unit = {
    val dbConfig = config.forDatasource(db)
    if (dbConfig.enabled) {
      val schema = dbConfig.schema

      if (evolutions.scripts(db, reader, schema).nonEmpty) {

        endpointsConfig.executeWithLock(db) {
          val scripts = evolutions.scripts(db, reader, schema)
          val hasDown = scripts.exists(_.isInstanceOf[DownScript])
          val autocommit = dbConfig.autocommit

          import Evolutions.toHumanReadableScript

          environment.mode match {
            case Mode.Test => evolutions.evolve(db, scripts, autocommit, schema)
            case Mode.Dev if dbConfig.autoApply => evolutions.evolve(db, scripts, autocommit, schema)
            case Mode.Prod if !hasDown && dbConfig.autoApply => evolutions.evolve(db, scripts, autocommit, schema)
            case Mode.Prod if hasDown && dbConfig.autoApply && dbConfig.autoApplyDowns => evolutions.evolve(db, scripts, autocommit, schema)
            case Mode.Prod if hasDown =>
              logger.warn(s"Your production database [$db] needs evolutions, including downs! \n\n${toHumanReadableScript(scripts)}")
              logger.warn(s"Run with -Dplay.evolutions.db.$db.autoApply=true and -Dplay.evolutions.db.$db.autoApplyDowns=true if you want to run them automatically, including downs (be careful, especially if your down evolutions drop existing data)")

              throw CassandraInvalidDatabaseRevision(db, toHumanReadableScript(scripts))

            case Mode.Prod =>
              logger.warn(s"Your production database [$db] needs evolutions! \n\n${toHumanReadableScript(scripts)}")
              logger.warn(s"Run with -Dplay.evolutions.db.$db.autoApply=true if you want to run them automatically (be careful)")

              throw CassandraInvalidDatabaseRevision(db, toHumanReadableScript(scripts))

            case _ => throw CassandraInvalidDatabaseRevision(db, toHumanReadableScript(scripts))
          }
        }
      }
    }
  }

  start()
}
