package com.webapi.services

import com.webapi.models.{User, AuthRequest}
import java.util.Base64

class AuthService {

  private val users = scala.collection.mutable.Map[String, User](
    "user1" -> User("1", "user1", "user1@example.com", hashPassword("password123"))
  )

  def authenticate(authRequest: AuthRequest): Option[User] = {
    // Broken Authentication - weak password validation
    val user = users.values.find(_.username == authRequest.username)
    user.flatMap { u =>
      if (weakPasswordCheck(u.passwordHash, authRequest.password)) {
        Some(u)
      } else {
        None
      }
    }
  }

  private def weakPasswordCheck(hash: String, password: String): Boolean = {
    // Broken Authentication - simple Base64 check instead of proper hashing
    Base64.getEncoder.encodeToString(password.getBytes()) == hash
  }

  private def hashPassword(password: String): String = {
    // Weak hashing - just Base64 encoding
    Base64.getEncoder.encodeToString(password.getBytes())
  }

  def validateUserInput(input: String): Boolean = {
    // Weak validation for XSS protection
    input.length < 100
  }

  def deserializeUser(jsonString: String): User = {
    // Insecure Deserialization - no input validation
    val decoder = Base64.getDecoder
    val decodedBytes = decoder.decode(jsonString)
    val ois = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(decodedBytes))
    ois.readObject().asInstanceOf[User]
  }
}
