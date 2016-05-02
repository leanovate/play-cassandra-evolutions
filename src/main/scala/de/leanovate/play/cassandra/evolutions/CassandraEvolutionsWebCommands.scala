package de.leanovate.play.cassandra.evolutions

import javax.inject.{Inject, Named, Singleton}

import play.api.db.evolutions.{EvolutionsApi, EvolutionsConfig, EvolutionsReader}
import play.core.HandleWebCommandSupport

@Singleton
class CassandraEvolutionsWebCommands @Inject()(
                                                @Named("cassandra") evolutions: EvolutionsApi,
                                                @Named("cassandra") reader: EvolutionsReader,
                                                config: EvolutionsConfig
                                              ) extends HandleWebCommandSupport {
  def handleWebCommand(request: play.api.mvc.RequestHeader, buildLink: play.core.BuildLink, path: java.io.File): Option[play.api.mvc.Result] = {
    val applyEvolutions = """/@cassandraEvolutions/apply/([a-zA-Z0-9_]+)""".r
    val resolveEvolutions = """/@cassandraEvolutions/resolve/([a-zA-Z0-9_]+)/([0-9]+)""".r

    lazy val redirectUrl = request.queryString.get("redirect").filterNot(_.isEmpty).map(_.head).getOrElse("/")

    request.path match {
      case applyEvolutions(db) =>
        Some {
          val scripts = evolutions.scripts(db, reader, config.forDatasource(db).schema)
          evolutions.evolve(db, scripts, config.forDatasource(db).autocommit, config.forDatasource(db).schema)
          buildLink.forceReload()
          play.api.mvc.Results.Redirect(redirectUrl)
        }

      case resolveEvolutions(db, rev) =>
        Some {
          evolutions.resolve(db, rev.toInt, config.forDatasource(db).schema)
          buildLink.forceReload()
          play.api.mvc.Results.Redirect(redirectUrl)
        }

      case _ => None
    }
  }
}
