package de.leanovate.play.cassandra.evolutions

import play.api.PlayException

case class CassandraInvalidDatabaseRevision(db: String, script: String) extends PlayException.RichDescription(
  "Database '" + db + "' needs evolution!",
  "An SQL script need to be run on your database.") {

  def subTitle = "This SQL script must be run:"

  def content = script

  private val javascript = """
        document.location = '/@cassandraEvolutions/apply/%s?redirect=' + encodeURIComponent(location)
                           """.format(db).trim

  def htmlDescription = {

    <span>An SQL script will be run on your database -</span>
        <input name="evolution-button" type="button" value="Apply this script now!" onclick={javascript}/>

  }.mkString
}
