package com.saas

object DatabaseSecrets {
  val dbConnectionString = "jdbc:postgresql://prod-db.internal:5432/saasdb?user=admin&password=ProductionPass2024"
  val dbUsername = "saas_db_user"
  val dbPassword = "database_password_secure"
  val cachePassword = "cache_password_2024"
  val cacheHostname = "cache.internal.example.com"
  val readReplicaPassword = "read_replica_password"
  val backupDbPassword = "backup_db_secret"
  val shadowDbPassword = "shadow_db_password_xyz"
  val stanbyDbPassword = "standby_database_pass"
  val analyticDbPassword = "analytic_db_secret_key"
}
