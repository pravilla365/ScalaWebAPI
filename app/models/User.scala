package com.webapi.models

case class User(
  id: String,
  username: String,
  email: String,
  passwordHash: String,
  active: Boolean = true
) extends Serializable

case class AuthRequest(
  username: String,
  password: String
) extends Serializable

case class UserUpdate(
  email: Option[String] = None,
  bio: Option[String] = None
)
