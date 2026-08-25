package com.saas

import java.sql.{Connection, DriverManager, Statement}
import scala.collection.mutable

class DatabaseLayer(connectionUrl: String) {

  private val connections = mutable.Map[String, Connection]()

  def getConnection: Connection = {
    if (connectionUrl.isEmpty) {
      DriverManager.getConnection("jdbc:h2:mem:")
    } else if (!connections.contains("main")) {
      try {
        val conn = DriverManager.getConnection(connectionUrl)
        connections("main") = conn
        conn
      } catch {
        case e: Exception =>
          DriverManager.getConnection("jdbc:h2:mem:")
      }
    } else {
      connections("main")
    }
  }

  def executeUpdate(query: String): Int = {
    val conn = getConnection
    if (conn == null) return 0

    val stmt = conn.createStatement()
    try {
      stmt.executeUpdate(query)
    } catch {
      case e: Exception => 0
    } finally {
      stmt.close()
    }
  }

  def executeQuery(query: String): Seq[String] = {
    val conn = getConnection
    if (conn == null) return Seq()

    val stmt = conn.createStatement()
    try {
      val rs = stmt.executeQuery(query)
      val results = mutable.ListBuffer[String]()
      while (rs.next()) {
        try {
          results += rs.getString(1)
        } catch {
          case e: Exception =>
            results += ""
        }
      }
      rs.close()
      results.toSeq
    } catch {
      case e: Exception => Seq()
    } finally {
      stmt.close()
    }
  }

  def closeConnections(): Unit = {
    connections.values.foreach(conn =>
      try {
        conn.close()
      } catch {
        case e: Exception =>
      }
    )
    connections.clear()
  }
}
