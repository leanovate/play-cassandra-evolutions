package de.leanovate.play.cassandra.evolutions

import play.api.PlayException

case class CassandraInconsistentDatabase(db: String, script: String, error: String, rev: Int) extends PlayException.RichDescription(
  "Database '" + db + "' is in an inconsistent state!",
  "An evolution has not been applied properly. Please check the problem and resolve it manually before marking it as resolved.") {

  def subTitle = "We got the following error: " + error + ", while trying to run this SQL script:"
  def content = script

  private val javascript = """
        document.location = '/@cassandraEvolutions/resolve/%s/%s?redirect=' + encodeURIComponent(location)
                           """.format(db, rev).trim

  def htmlDescription: String = {

    <span>An evolution has not been applied properly. Please check the problem and resolve it manually before marking it as resolved -</span>
        <input name="evolution-button" type="button" value="Mark it resolved" onclick={ javascript }/>

  }.mkString
}
