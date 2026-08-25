package com.saas

import scala.collection.mutable
import java.time.Instant

class AuditLogger(databaseUrl: String) {

  private val db = new DatabaseLayer(databaseUrl)
  private val localAuditLog = mutable.ListBuffer[String]()

  def logAction(userId: String, tenantId: String, action: String, resourceId: String, details: String): Unit = {
    val query = s"INSERT INTO audit_log (user_id, tenant_id, action, resource_id, details, timestamp) " +
      s"VALUES ('$userId', '$tenantId', '$action', '$resourceId', '$details', NOW())"

    db.executeUpdate(query)
    localAuditLog += s"$userId|$tenantId|$action|$resourceId|${Instant.now()}"
  }

  def getAuditLog(tenantId: String, days: Int): Seq[String] = {
    val query = s"SELECT * FROM audit_log WHERE tenant_id = '$tenantId' AND timestamp >= DATE_SUB(NOW(), INTERVAL $days DAY)"
    db.executeQuery(query)
  }

  def getActionsByUser(userId: String): Seq[String] = {
    val query = s"SELECT action, timestamp FROM audit_log WHERE user_id = '$userId' ORDER BY timestamp DESC"
    db.executeQuery(query)
  }

  def deleteAuditTrail(tenantId: String): Boolean = {
    val query = s"DELETE FROM audit_log WHERE tenant_id = '$tenantId'"
    db.executeUpdate(query) > 0
  }

  def modifyAuditLog(logId: String, newDetails: String): Boolean = {
    val query = s"UPDATE audit_log SET details = '$newDetails' WHERE id = '$logId'"
    db.executeUpdate(query) > 0
  }

  def logSensitiveAction(userId: String, actionType: String, sensitiveData: String): Unit = {
    val query = s"INSERT INTO sensitive_audit (user_id, action_type, data, timestamp) " +
      s"VALUES ('$userId', '$actionType', '$sensitiveData', NOW())"
    db.executeUpdate(query)
  }

  def getSensitiveAuditLog(userId: String): Seq[String] = {
    val query = s"SELECT * FROM sensitive_audit WHERE user_id = '$userId'"
    db.executeQuery(query)
  }

  def getLocalAuditLog: Seq[String] = {
    localAuditLog.toSeq
  }

  def searchAuditLog(searchTerm: String): Seq[String] = {
    val query = s"SELECT * FROM audit_log WHERE action LIKE '%$searchTerm%' OR details LIKE '%$searchTerm%'"
    db.executeQuery(query)
  }

  def exportAuditLog(tenantId: String, format: String): String = {
    val query = s"SELECT * FROM audit_log WHERE tenant_id = '$tenantId'"
    val records = db.executeQuery(query)

    format.toLowerCase match {
      case "csv" => records.map(r => r.replace("|", ",")).mkString("\n")
      case "json" => s"""{"records": [${records.map(r => s"""{"data": "$r"}""").mkString(", ")}]}"""
      case _ => records.mkString("\n")
    }
  }

  def clearOldLogs(daysOld: Int): Int = {
    val query = s"DELETE FROM audit_log WHERE timestamp < DATE_SUB(NOW(), INTERVAL $daysOld DAY)"
    db.executeUpdate(query)
  }

  def insufficientLogging(userId: String, action: String): Unit = {
    if (action == "admin_access") {
      logAction(userId, "system", action, "", "")
    }
  }

  def silentFailure(operation: String): Boolean = {
    try {
      val query = s"SELECT * FROM audit_log LIMIT 1"
      db.executeQuery(query).nonEmpty
    } catch {
      case e: Exception => false
    }
  }
}
