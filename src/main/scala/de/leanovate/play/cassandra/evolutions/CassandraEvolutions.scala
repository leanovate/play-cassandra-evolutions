package de.leanovate.play.cassandra.evolutions

import java.sql.SQLException
import java.util.Date

import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.querybuilder.QueryBuilder.{eq => eql, _}
import com.datastax.driver.core.{Cluster, Session}
import play.api.Logger
import play.api.db.evolutions._

import scala.collection.JavaConversions._
import scala.util.control.NonFatal

class CassandraEvolutions(name: String, endpointConfig: CassandraEndpointConfig) {
  val evolutionsKeyspace = endpointConfig.evolutionKeyspaceForDatabase(name)

  import CassandraEvolutions._

  def evolve(scripts: Seq[Script], autocommit: Boolean): Unit = {
    def logBefore(script: Script)(implicit session: Session) {
      script match {
        case UpScript(e) =>
          session.execute(
            QueryBuilder.insertInto(evolutionsKeyspace, "play_evolutions")
              .value("id", e.revision)
              .value("hash", e.hash)
              .value("applied_at", new Date)
              .value("apply_script", e.sql_up)
              .value("revert_script", e.sql_down)
              .value("state", "applying_up")
              .value("last_problem", "")
          )
        case DownScript(e) =>
          session.execute(
            QueryBuilder.update(evolutionsKeyspace, "play_evolutions")
              .`with`(set("state", "applied"))
              .where(eql("id", e.revision))
          )
      }
    }

    def logAfter(script: Script)(implicit session: Session): Unit = {
      script match {
        case UpScript(e) =>
          session.execute(
            QueryBuilder.update(evolutionsKeyspace, "play_evolutions")
              .`with`(set("state", "applied"))
              .where(eql("id", e.revision))
          )
        case DownScript(e) =>
          session.execute(
            QueryBuilder.delete().all()
              .from(evolutionsKeyspace, "play_evolutions")
              .where(eql("id", e.revision))
          )
      }
    }

    def updateLastProblem(message: String, revision: Int)(implicit session: Session): Unit = {
      session.execute(
        QueryBuilder.update(evolutionsKeyspace, "play_evolutions")
          .`with`(set("last_problem", message))
          .where(eql("id", revision))
      )
    }

    checkEvolutionsState()

    implicit val session = endpointConfig.sessionForDatabase(name)

    var applying = -1
    var lastScript: Script = null

    try {
      scripts.foreach { script =>
        lastScript = script
        applying = script.evolution.revision
        logBefore(script)
        // Execute script
        script.statements.foreach(session.execute)
        logAfter(script)
      }
    } catch {
      case NonFatal(e) =>
        val message = e match {
          case ex: SQLException => ex.getMessage + " [ERROR:" + ex.getErrorCode + ", SQLSTATE:" + ex.getSQLState + "]"
          case ex => ex.getMessage
        }
        if (!autocommit) {
          logger.error(message)

          val humanScript = "# --- Rev:" + lastScript.evolution.revision + "," + (if (lastScript.isInstanceOf[UpScript]) "Ups" else "Downs") + " - " + lastScript.evolution.hash + "\n\n" + (if (lastScript.isInstanceOf[UpScript]) lastScript.evolution.sql_up else lastScript.evolution.sql_down)

          throw InconsistentDatabase(name, humanScript, message, lastScript.evolution.revision, true)
        } else {
          updateLastProblem(message, applying)
        }
    } finally {
      session.close()
    }
  }

  def scripts(evolutions: Seq[Evolution]): Seq[Script] = {
    if (evolutions.nonEmpty) {
      val application = evolutions.reverse
      val database = databaseEvolutions()

      val (nonConflictingDowns, dRest) = database.span(e => !application.headOption.exists(e.revision <= _.revision))
      val (nonConflictingUps, uRest) = application.span(e => !database.headOption.exists(_.revision >= e.revision))

      val (conflictingDowns, conflictingUps) = Evolutions.conflictings(dRest, uRest)

      val ups = (nonConflictingUps ++ conflictingUps).reverseMap(e => UpScript(e))
      val downs = (nonConflictingDowns ++ conflictingDowns).map(e => DownScript(e))

      downs ++ ups
    } else Nil
  }

  def scripts(reader: EvolutionsReader): Seq[Script] = scripts(reader.evolutions(name))

  def resetScripts(): Seq[Script] = {
    val appliedEvolutions = databaseEvolutions()
    appliedEvolutions.map(DownScript)
  }

  def resolve(revision: Int) = {
    implicit val session = endpointConfig.sessionForDatabase(name)

    try {
      session.execute(
        QueryBuilder.update(evolutionsKeyspace, "play_evolutions")
          .`with`(set("state", "applied"))
          .where(eql("state", "applying_up"))
          .and(eql("id", revision))
      )
      session.execute(
        QueryBuilder.delete().all()
          .from(evolutionsKeyspace, "play_evolutions")
          .where(eql("state", "applying_down"))
          .and(eql("id", revision))
      )
    } finally {
      session.close()
    }
  }

  private def databaseEvolutions(): Seq[Evolution] = {
    checkEvolutionsState()

    implicit val session = endpointConfig.sessionForDatabase(name)

    try {
      session.execute(
        QueryBuilder.select().all()
          .from(evolutionsKeyspace, "play_evolutions")
      ).all().map {
        row =>
          Evolution(
            row.getInt("id"),
            row.getString("apply_script"),
            row.getString("revert_script")
          )
      }.sortBy(_.revision).reverse
    } finally {
      session.close()
    }
  }

  private def checkEvolutionsState() {
    def createPlayEvolutionsTable()(implicit session: Session): Unit = {
      try {
        createPlayEvolutionsCql(evolutionsKeyspace).foreach(session.execute)
      } catch {
        case NonFatal(ex) => logger.warn("could not create play_evolutions table", ex)
      }
    }

    implicit val session = endpointConfig.sessionForDatabase(name)
    try {
      session.execute(
        QueryBuilder.select().all()
          .from(evolutionsKeyspace, "play_evolutions")
          .where(in("state", "applying_up", "applying_down"))
      ).all().headOption.foreach {
        problem =>
          val revision = problem.getInt("id")
          val state = problem.getString("state")
          val hash = problem.getString("hash").take(7)
          val script = state match {
            case "applying_up" => problem.getString("apply_script")
            case _ => problem.getString("revert_script")
          }

          val error = problem.getString("last_problem")

          logger.error(error)

          val humanScript = "# --- Rev:" + revision + "," + (if (state == "applying_up") "Ups" else "Downs") + " - " + hash + "\n\n" + script

          throw CassandraInconsistentDatabase(name, humanScript, error, revision)
      }
    } catch {
      case e: CassandraInconsistentDatabase => throw e
      case NonFatal(_) => createPlayEvolutionsTable()
    } finally {
      session.close()
    }
  }
}

object CassandraEvolutions {
  val logger = Logger(classOf[CassandraEvolutions])

  def fileName(db: String, revision: Int): String = s"${Evolutions.directoryName(db)}/$revision.cql"

  def resourceName(db: String, revision: Int): String = s"evolutions/$db/$revision.cql"

  def createPlayEvolutionsCql(keySpace: String) = Seq(
    s"""
      CREATE KEYSPACE IF NOT EXISTS $keySpace WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1}
    """,
    s"""
      CREATE TABLE IF NOT EXISTS $keySpace.play_evolutions (
          id int primary key,
          hash varchar,
          applied_at timestamp,
          apply_script text,
          revert_script text,
          state varchar,
          last_problem text
      )
    """)
}