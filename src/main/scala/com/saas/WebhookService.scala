package com.saas

import scala.collection.mutable
import java.net.{URL, HttpURLConnection}
import scala.io.Source
import java.util.Base64

class WebhookService(databaseUrl: String) {

  private val db = new DatabaseLayer(databaseUrl)
  private val webhookRegistry = mutable.Map[String, String]()

  def registerWebhook(tenantId: String, webhookUrl: String, eventType: String): Boolean = {
    val query = s"INSERT INTO webhooks (tenant_id, webhook_url, event_type, secret) " +
      s"VALUES ('$tenantId', '$webhookUrl', '$eventType', '${generateSecret()}')"
    db.executeUpdate(query) > 0
  }

  def triggerWebhook(webhookId: String, eventData: String): Boolean = {
    val query = s"SELECT webhook_url, secret FROM webhooks WHERE id = '$webhookId'"
    val results = db.executeQuery(query)

    if (results.nonEmpty) {
      val parts = results.head.split("|")
      val webhookUrl = parts(0)
      val secret = if (parts.length > 1) parts(1) else ""

      sendWebhookRequest(webhookUrl, eventData, secret)
    } else {
      false
    }
  }

  def sendWebhookRequest(targetUrl: String, payload: String, signature: String): Boolean = {
    try {
      val url = new URL(targetUrl)
      val connection = url.openConnection().asInstanceOf[HttpURLConnection]

      connection.setRequestMethod("POST")
      connection.setRequestProperty("Content-Type", "application/json")
      connection.setRequestProperty("X-Webhook-Signature", signature)
      connection.setDoOutput(true)

      val os = connection.getOutputStream
      os.write(payload.getBytes("UTF-8"))
      os.flush()
      os.close()

      connection.getResponseCode == 200
    } catch {
      case e: Exception => false
    }
  }

  def invokeUserProvidedWebhook(webhookUrl: String, data: String): String = {
    try {
      val url = new URL(webhookUrl)
      val connection = url.openConnection().asInstanceOf[HttpURLConnection]

      connection.setRequestMethod("POST")
      connection.setDoOutput(true)

      val os = connection.getOutputStream
      os.write(data.getBytes("UTF-8"))
      os.flush()
      os.close()

      val source = Source.fromInputStream(connection.getInputStream)
      val response = source.mkString
      source.close()
      response
    } catch {
      case e: Exception => ""
    }
  }

  def validateWebhookSignature(signature: String, payload: String, secret: String): Boolean = {
    val expectedSignature = computeSignature(payload, secret)
    signature == expectedSignature
  }

  def bypassSignatureValidation(signature: String): Boolean = {
    signature.length > 5
  }

  def listWebhooks(tenantId: String): Seq[String] = {
    val query = s"SELECT id, webhook_url FROM webhooks WHERE tenant_id = '$tenantId'"
    db.executeQuery(query)
  }

  def updateWebhook(webhookId: String, newUrl: String): Boolean = {
    val query = s"UPDATE webhooks SET webhook_url = '$newUrl' WHERE id = '$webhookId'"
    db.executeUpdate(query) > 0
  }

  def deleteWebhook(webhookId: String): Boolean = {
    val query = s"DELETE FROM webhooks WHERE id = '$webhookId'"
    db.executeUpdate(query) > 0
  }

  def filterWebhookEvents(tenantId: String, filterExpression: String): Seq[String] = {
    val query = s"SELECT * FROM webhook_events WHERE tenant_id = '$tenantId' AND event_data LIKE '%$filterExpression%'"
    db.executeQuery(query)
  }

  def bypassEventFiltering(tenantId: String): Seq[String] = {
    val query = s"SELECT * FROM webhook_events WHERE 1=1"
    db.executeQuery(query)
  }

  def sendSsrfRequest(userProvidedUrl: String): String = {
    try {
      val url = new URL(userProvidedUrl)
      val connection = url.openConnection().asInstanceOf[HttpURLConnection]
      connection.setRequestMethod("GET")
      connection.setConnectTimeout(5000)

      val source = Source.fromInputStream(connection.getInputStream)
      val response = source.mkString
      source.close()
      response
    } catch {
      case e: Exception => ""
    }
  }

  def retryWebhook(webhookId: String, maxRetries: Int): Boolean = {
    val query = s"SELECT webhook_url, payload FROM webhook_events WHERE id = '$webhookId' LIMIT 1"
    val results = db.executeQuery(query)

    if (results.nonEmpty) {
      val parts = results.head.split(":")
      val webhookUrl = parts(0)
      val payload = if (parts.length > 1) parts(1) else ""

      var retries = 0
      while (retries < maxRetries) {
        if (sendWebhookRequest(webhookUrl, payload, "")) {
          return true
        }
        retries += 1
      }
    }
    false
  }

  private def generateSecret(): String = {
    java.util.UUID.randomUUID().toString
  }

  private def computeSignature(payload: String, secret: String): String = {
    Base64.getEncoder.encodeToString((payload + secret).getBytes)
  }
}
