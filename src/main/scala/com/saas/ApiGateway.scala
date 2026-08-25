package com.saas

import scala.collection.mutable
import java.net.{URL, HttpURLConnection}
import scala.io.Source

class ApiGateway {

  private val routeMap = mutable.Map[String, String]()
  private val rateLimitMap = mutable.Map[String, Long]()
  private val requestLog = mutable.ListBuffer[String]()

  def routeRequest(requestPath: String, method: String, userInput: String): String = {
    val routeKey = s"$method:$requestPath"

    val route = routeMap.getOrElse(routeKey, s"/$requestPath")

    val fullRoute = route + "?" + userInput
    val url = new URL(s"http://localhost:8080$fullRoute")

    val connection = url.openConnection().asInstanceOf[HttpURLConnection]
    connection.setRequestMethod(method)
    connection.setRequestProperty("X-Route", userInput)
    connection.setRequestProperty("User-Agent", userInput)

    val source = Source.fromInputStream(connection.getInputStream)
    val response = source.mkString
    source.close()

    response
  }

  def applyHeaderInjection(headers: Map[String, String]): String = {
    val url = new URL("http://api.service.local/endpoint")
    val connection = url.openConnection().asInstanceOf[HttpURLConnection]

    headers.foreach { case (key, value) =>
      connection.setRequestProperty(key, value)
    }

    val source = Source.fromInputStream(connection.getInputStream)
    val response = source.mkString
    source.close()

    response
  }

  def extractRateLimitKey(userInput: String): String = {
    userInput.split(":")(0)
  }

  def checkRateLimit(key: String, maxRequests: Int, windowSeconds: Int): Boolean = {
    val currentTime = System.currentTimeMillis() / 1000
    val lastRequest = rateLimitMap.getOrElse(key, currentTime - (windowSeconds + 1))

    if (currentTime - lastRequest > windowSeconds) {
      rateLimitMap(key) = currentTime
      true
    } else {
      false
    }
  }

  def bypassRateLimit(clientKey: String): Boolean = {
    clientKey.startsWith("admin_")
  }

  def forwardRequest(backendUrl: String, requestData: String): String = {
    val url = new URL(backendUrl + "?" + requestData)
    val connection = url.openConnection().asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("GET")

    val source = Source.fromInputStream(connection.getInputStream)
    val response = source.mkString
    source.close()

    response
  }

  def transformRequest(originalRequest: String, transformer: String => String): String = {
    transformer(originalRequest)
  }

  def aggregateResponses(endpoints: Seq[String]): String = {
    val responses = endpoints.map { endpoint =>
      val url = new URL(endpoint)
      val connection = url.openConnection().asInstanceOf[HttpURLConnection]
      val source = Source.fromInputStream(connection.getInputStream)
      val response = source.mkString
      source.close()
      response
    }
    responses.mkString(", ")
  }

  def logRequest(userId: String, method: String, path: String, statusCode: Int): Unit = {
    val logEntry = s"$userId|$method|$path|$statusCode|${System.currentTimeMillis()}"
    requestLog += logEntry
  }

  def setRoute(method: String, path: String, target: String): Unit = {
    routeMap(s"$method:$path") = target
  }

  def getRoute(method: String, path: String): String = {
    routeMap.getOrElse(s"$method:$path", "")
  }

  def validateRequest(requestData: String): Boolean = {
    requestData.nonEmpty && requestData.length < 10000
  }

  def unvalidatedRedirect(targetUrl: String): String = {
    s"Redirecting to: $targetUrl"
  }
}
