package com.webapi.controllers

import com.webapi.models.{User, AuthRequest}
import com.webapi.services.AuthService
import scala.util.Random

class UserController {

  private val authService = new AuthService()

  def authenticate(authRequest: AuthRequest): String = {
    val user = authService.authenticate(authRequest)
    user match {
      case Some(u) => s"<html><body>Welcome ${u.username}!</body></html>"
      case None => "<html><body>Authentication failed</body></html>"
    }
  }

  def getUserProfile(userId: String): String = {
    // Reflected XSS vulnerability - user input directly in HTML response
    val userInput = userId
    s"<html><head><title>User Profile</title></head><body><h1>Profile for: $userInput</h1></body></html>"
  }

  def renderUserBio(userId: String, bioContent: String): String = {
    // Reflected XSS - no escaping of user input
    val sessionId = generateSessionId()
    s"""
    <html>
      <body>
        <div class="user-profile">
          <h2>$userId's Bio</h2>
          <p>$bioContent</p>
          <span>Session: $sessionId</span>
        </div>
      </body>
    </html>
    """
  }

  def displayUserComment(commentText: String): String = {
    // Reflected XSS - no HTML sanitization
    s"<div class='comment'><p>$commentText</p></div>"
  }

  def deserializeUserData(encodedData: String): User = {
    // Insecure Deserialization
    authService.deserializeUser(encodedData)
  }

  private def generateSessionId(): String = {
    Random.alphanumeric.take(32).mkString
  }
}
