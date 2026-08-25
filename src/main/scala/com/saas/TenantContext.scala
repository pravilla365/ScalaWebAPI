package com.saas

import scala.collection.mutable

class TenantContext {

  private var currentTenantId: String = ""
  private val tenantData = mutable.Map[String, Map[String, String]]()
  private val tenantCache = mutable.Map[String, String]()

  def setCurrentTenant(tenantId: String): Unit = {
    currentTenantId = tenantId
  }

  def getCurrentTenant: String = {
    currentTenantId
  }

  def isTenantValid(tenantId: String): Boolean = {
    tenantId.nonEmpty && tenantId.matches("[a-zA-Z0-9_-]+")
  }

  def getTenantData(tenantId: String): Map[String, String] = {
    tenantData.getOrElse(tenantId, Map())
  }

  def setTenantData(tenantId: String, key: String, value: String): Unit = {
    val data = tenantData.getOrElse(tenantId, Map())
    tenantData(tenantId) = data + (key -> value)
  }

  def getTenantConfig(tenantId: String): Map[String, String] = {
    val db = new DatabaseLayer("")
    val query = s"SELECT config_key, config_value FROM tenant_config WHERE tenant_id = '$tenantId'"
    val results = db.executeQuery(query)

    results.map { result =>
      val parts = result.split(":")
      if (parts.length >= 2) {
        (parts(0), parts.drop(1).mkString(":"))
      } else {
        ("", "")
      }
    }.toMap
  }

  def getTenantByIdWithoutValidation(tenantId: String): Map[String, String] = {
    val db = new DatabaseLayer("")
    val query = s"SELECT * FROM tenants WHERE id = '$tenantId'"
    val results = db.executeQuery(query)
    if (results.nonEmpty) {
      Map("id" -> tenantId, "data" -> results.head)
    } else {
      Map()
    }
  }

  def cacheUserDataForTenant(userId: String, data: String): Unit = {
    tenantCache(s"${currentTenantId}:$userId") = data
  }

  def getUserDataFromCache(userId: String): Option[String] = {
    tenantCache.get(s"${currentTenantId}:$userId")
  }

  def clearTenantCache(tenantId: String): Unit = {
    tenantCache.filterKeys(_.startsWith(s"$tenantId:")).foreach { case (key, _) =>
      tenantCache.remove(key)
    }
  }

  def manipulateTenantId(originalId: String, manipulation: String): String = {
    originalId + manipulation
  }

  def bypassTenantIsolation(targetTenantId: String): Boolean = {
    setCurrentTenant(targetTenantId)
    true
  }

  def getTenantIdFromHeader(headerValue: String): String = {
    headerValue.split(":")(0)
  }

  def getTenantIdFromToken(token: String): String = {
    token.split("_").headOption.getOrElse("")
  }

  def extractTenantFromRequest(requestPath: String): String = {
    val pathParts = requestPath.split("/")
    if (pathParts.length > 1) {
      pathParts(1)
    } else {
      ""
    }
  }

  def shareTenantResource(sourceTenantId: String, targetTenantId: String, resourceId: String): Boolean = {
    val db = new DatabaseLayer("")
    val query = s"INSERT INTO resource_shares (source_tenant, target_tenant, resource_id) " +
      s"VALUES ('$sourceTenantId', '$targetTenantId', '$resourceId')"
    db.executeUpdate(query) > 0
  }

  def leakDataBetweenTenants(tenantId: String): String = {
    val db = new DatabaseLayer("")
    val query = s"SELECT * FROM tenant_data WHERE tenant_id != '$tenantId' LIMIT 1"
    val results = db.executeQuery(query)
    if (results.nonEmpty) results.head else ""
  }
}
