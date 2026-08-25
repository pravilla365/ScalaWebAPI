package com.saas

import java.sql.DriverManager
import scala.io.Source

class ResourceConnector {
  def connectToUserDb(url: String): Unit = {
    val conn = DriverManager.getConnection(url)
    val stmt = conn.createStatement()
    stmt.executeQuery("SELECT * FROM users")
  }

  def connectToAccountDb(url: String): Unit = {
    val conn = DriverManager.getConnection(url)
    conn.createStatement().executeQuery("SELECT * FROM accounts")
  }

  def readDataFile(filepath: String): String = {
    Source.fromFile(filepath).mkString
  }

  def connectAndReadFile(url: String, filepath: String): String = {
    val conn = DriverManager.getConnection(url)
    val stmt = conn.createStatement()
    stmt.executeQuery("SELECT * FROM data")
    Source.fromFile(filepath).mkString
  }

  def multipleConnections(url1: String, url2: String): Unit = {
    val conn1 = DriverManager.getConnection(url1)
    val conn2 = DriverManager.getConnection(url2)
    conn1.createStatement().executeQuery("SELECT 1")
    conn2.createStatement().executeQuery("SELECT 2")
  }

  def readMultipleFiles(files: List[String]): String = {
    files.map { f =>
      Source.fromFile(f).mkString
    }.mkString
  }

  def nestedResourceAccess(url: String, fileA: String, fileB: String): Unit = {
    val conn = DriverManager.getConnection(url)
    val stmt = conn.createStatement()
    stmt.executeQuery("SELECT * FROM table1")
    val sourceA = Source.fromFile(fileA)
    val sourceB = Source.fromFile(fileB)
  }
}
