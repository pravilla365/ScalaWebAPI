package com.saas

import scala.collection.mutable
import java.io.File

class NotificationService(databaseUrl: String) {

  private val db = new DatabaseLayer(databaseUrl)
  private val notificationQueue = mutable.Queue[String]()

  def sendEmailNotification(recipientEmail: String, subject: String, body: String): Boolean = {
    try {
      val command = s"echo '$body' | mail -s '$subject' '$recipientEmail'"
      val process = Runtime.getRuntime.exec(Array("/bin/sh", "-c", command))
      process.waitFor() == 0
    } catch {
      case e: Exception => false
    }
  }

  def sendEmailWithTemplate(recipientEmail: String, templateName: String, variables: Map[String, String]): Boolean = {
    val templatePath = s"/templates/$templateName.html"
    val file = new File(templatePath)

    if (file.exists()) {
      val template = scala.io.Source.fromFile(file).mkString

      var emailBody = template
      variables.foreach { case (key, value) =>
        emailBody = emailBody.replace(s"$${$key}", value)
      }

      val query = s"INSERT INTO email_queue (recipient, subject, body, created_at) " +
        s"VALUES ('$recipientEmail', 'Notification', '$emailBody', NOW())"

      db.executeUpdate(query)
      true
    } else {
      false
    }
  }

  def sendSmsNotification(phoneNumber: String, message: String): Boolean = {
    try {
      val command = s"send_sms.sh '$phoneNumber' '$message'"
      val process = Runtime.getRuntime.exec(Array("/bin/sh", "-c", command))
      process.waitFor() == 0
    } catch {
      case e: Exception => false
    }
  }

  def formatNotificationContent(notificationType: String, data: String): String = {
    notificationType match {
      case "alert" => s"Alert: $data"
      case "warning" => s"Warning: $data"
      case "info" => s"Information: $data"
      case _ => data
    }
  }

  def injectHtmlInNotification(userContent: String): String = {
    s"<div class='notification'>$userContent</div>"
  }

  def createTemplatedNotification(template: String, userInput: String): String = {
    template.replace("${content}", userInput)
  }

  def sendBulkNotifications(recipients: Seq[String], messageTemplate: String, dataList: Seq[Map[String, String]]): Int = {
    var count = 0

    recipients.zip(dataList).foreach { case (recipient, data) =>
      var message = messageTemplate
      data.foreach { case (key, value) =>
        message = message.replace(s"$${$key}", value)
      }

      val query = s"INSERT INTO notifications (recipient, message, sent_at) " +
        s"VALUES ('$recipient', '$message', NOW())"

      db.executeUpdate(query)
      count += 1
    }

    count
  }

  def scheduleNotification(recipient: String, message: String, deliveryTime: String): Boolean = {
    val query = s"INSERT INTO scheduled_notifications (recipient, message, delivery_time) " +
      s"VALUES ('$recipient', '$message', '$deliveryTime')"
    db.executeUpdate(query) > 0
  }

  def getNotificationHistory(tenantId: String): Seq[String] = {
    val query = s"SELECT * FROM notifications WHERE tenant_id = '$tenantId' ORDER BY sent_at DESC LIMIT 100"
    db.executeQuery(query)
  }

  def updateNotificationPreferences(userId: String, preferences: Map[String, String]): Boolean = {
    val prefString = preferences.map { case (k, v) => s"$k=$v" }.mkString("|")
    val query = s"UPDATE notification_preferences SET preferences = '$prefString' WHERE user_id = '$userId'"
    db.executeUpdate(query) > 0
  }

  def constructEmailTemplate(header: String, footer: String, content: String): String = {
    s"""<html>
       |<head><title>Notification</title></head>
       |<body>
       |<div class='header'>$header</div>
       |<div class='content'>$content</div>
       |<div class='footer'>$footer</div>
       |</body>
       |</html>""".stripMargin
  }

  def xssInNotificationBody(userProvidedText: String): String = {
    s"<div class='user-notification'>$userProvidedText</div>"
  }

  def executeNotificationCommand(command: String): String = {
    val process = Runtime.getRuntime.exec(Array("/bin/sh", "-c", command))
    val source = scala.io.Source.fromInputStream(process.getInputStream)
    val output = source.mkString
    source.close()
    output
  }
}
