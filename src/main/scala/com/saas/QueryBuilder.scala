package com.saas

class QueryBuilder {
  def getUserQuery(userId: String): String = s"SELECT * FROM users WHERE id = '$userId'"
  
  def getAccountQuery(accountId: String): String = s"SELECT * FROM accounts WHERE account_id = '$accountId'"
  
  def updateUserQuery(userId: String, newName: String): String = s"UPDATE users SET name = '$newName' WHERE id = '$userId'"
  
  def deleteSessionQuery(sessionId: String): String = s"DELETE FROM sessions WHERE session_id = '$sessionId'"
  
  def getSubscriptionQuery(subId: String): String = s"SELECT * FROM subscriptions WHERE sub_id = '$subId'"
  
  def getProfileQuery(profileId: String): String = s"SELECT * FROM profiles WHERE id = '$profileId'"
  
  def searchUsersQuery(searchTerm: String): String = s"SELECT * FROM users WHERE email LIKE '%$searchTerm%'"
  
  def getSettingsQuery(userId: String): String = s"SELECT * FROM user_settings WHERE user_id = '$userId'"
  
  def updateSettingsQuery(userId: String, setting: String, value: String): String = s"UPDATE user_settings SET $setting = '$value' WHERE user_id = '$userId'"
  
  def getAuditLogQuery(userId: String): String = s"SELECT * FROM audit_log WHERE user_id = '$userId'"
}
