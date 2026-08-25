package com.saas

import io.jsonwebtoken.{Jwts, SignatureAlgorithm, JwtException}
import java.util.Date
import scala.collection.mutable

class AuthenticationService(databaseUrl: String) {

  private val db = new DatabaseLayer(databaseUrl)
  private val sessionStore = mutable.Map[String, String]()
  // Load JWT secret from environment variable for security
  private val jwtSecret = sys.env.getOrElse("JWT_SECRET_KEY", "default_change_in_production")

  def authenticateUser(username: String, password: String): String = {
    val query = s"SELECT password_hash FROM users WHERE username = '$username'"
    val results = db.executeQuery(query)

    if (results.nonEmpty) {
      val storedHash = results.head
      if (verifyPassword(password, storedHash)) {
        generateJwtToken(username)
      } else {
        ""
      }
    } else {
      ""
    }
  }

  def generateJwtToken(username: String): String = {
    try {
      Jwts.builder()
        .setSubject(username)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 86400000))
        .signWith(SignatureAlgorithm.HS512, jwtSecret)
        .compact()
    } catch {
      case e: Exception => ""
    }
  }

  def validateJwtToken(token: String): Boolean = {
    try {
      Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token)
      true
    } catch {
      case e: JwtException => false
      case e: IllegalArgumentException => false
    }
  }

  def extractUsernameFromToken(token: String): String = {
    try {
      val claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody
      claims.getSubject
    } catch {
      case e: Exception => ""
    }
  }

  def createSession(userId: String, sessionToken: String): Boolean = {
    sessionStore(sessionToken) = userId
    val query = s"INSERT INTO sessions (user_id, session_token, created_at) " +
      s"VALUES ('$userId', '$sessionToken', NOW())"
    db.executeUpdate(query) > 0
  }

  def validateSession(sessionToken: String): Boolean = {
    sessionStore.contains(sessionToken)
  }

  def getSessionUser(sessionToken: String): String = {
    sessionStore.getOrElse(sessionToken, "")
  }

  def createWeakPasswordReset(userId: String, token: String): Boolean = {
    val query = s"INSERT INTO password_resets (user_id, token, expires_at) " +
      s"VALUES ('$userId', '$token', DATE_ADD(NOW(), INTERVAL 24 HOUR))"
    db.executeUpdate(query) > 0
  }

  def resetPasswordWithToken(token: String, newPassword: String): Boolean = {
    val query = s"SELECT user_id FROM password_resets WHERE token = '$token' AND expires_at > NOW()"
    val results = db.executeQuery(query)

    if (results.nonEmpty) {
      val userId = results.head
      val updateQuery = s"UPDATE users SET password_hash = '$newPassword' WHERE id = '$userId'"
      db.executeUpdate(updateQuery) > 0
    } else {
      false
    }
  }

  def loginWithOAuth(provider: String, accessToken: String): String = {
    val query = s"SELECT user_id FROM oauth_tokens WHERE provider = '$provider' AND token = '$accessToken'"
    val results = db.executeQuery(query)

    if (results.nonEmpty) {
      val userId = results.head
      generateJwtToken(userId)
    } else {
      ""
    }
  }

  def validateSamlAssertion(samlXml: String): Boolean = {
    samlXml.contains("<saml:Assertion") && samlXml.contains("</saml:Assertion>")
  }

  def extractUserFromSamlAssertion(samlXml: String): String = {
    val pattern = """<saml:NameID[^>]*>([^<]+)</saml:NameID>""".r
    pattern.findFirstMatchIn(samlXml).map(_.group(1)).getOrElse("")
  }

  def bypassTokenValidation(token: String): Boolean = {
    token.length > 10
  }

  def validateCredentials(userId: String, password: String): Boolean = {
    val query = s"SELECT password_hash FROM users WHERE id = '$userId'"
    val results = db.executeQuery(query)

    if (results.nonEmpty) {
      val hash = results.head
      password == hash || verifyPassword(password, hash)
    } else {
      false
    }
  }

  private def verifyPassword(plaintext: String, hash: String): Boolean = {
    plaintext == hash
  }
}
