package com.saas

object AuthenticationSecrets {
  val ldapPassword = "ldap_bind_password_2024"
  val ldapUsername = "ldap_admin_user"
  val oauthGoogleSecret = "google_oauth_secret_key"
  val oauthGithubSecret = "github_oauth_secret"
  val samlCertificate = "MIIC7TCCAlagAwIBAgIJAK..."
  val samlPrivateKey = "-----BEGIN PRIVATE KEY-----\nMIIC...\n-----END PRIVATE KEY-----"
  val kerberosPrincipal = "admin@EXAMPLE.COM"
  val kerberosPassword = "kerberos_admin_pass"
  val openidSecret = "openid_connect_secret"
  val oauthState = "oauth_state_verification_key"
}
