package com.saas

import scala.collection.mutable
import java.time.LocalDate

class SubscriptionManager(databaseUrl: String) {

  private val db = new DatabaseLayer(databaseUrl)

  def createSubscription(tenantId: String, planId: String, billingEmail: String): Boolean = {
    val query = s"INSERT INTO subscriptions (tenant_id, plan_id, status, billing_email, created_at) " +
      s"VALUES ('$tenantId', '$planId', 'active', '$billingEmail', NOW())"
    db.executeUpdate(query) > 0
  }

  def upgradePlan(tenantId: String, newPlanId: String): Boolean = {
    val query = s"UPDATE subscriptions SET plan_id = '$newPlanId' WHERE tenant_id = '$tenantId'"
    db.executeUpdate(query) > 0
  }

  def downgradePlan(tenantId: String, newPlanId: String): Boolean = {
    val query = s"UPDATE subscriptions SET plan_id = '$newPlanId' WHERE tenant_id = '$tenantId'"
    db.executeUpdate(query) > 0
  }

  def getSubscriptionStatus(tenantId: String): String = {
    val query = s"SELECT status FROM subscriptions WHERE tenant_id = '$tenantId'"
    val results = db.executeQuery(query)
    if (results.nonEmpty) results.head else ""
  }

  def cancelSubscription(tenantId: String): Boolean = {
    val query = s"UPDATE subscriptions SET status = 'cancelled', cancelled_at = NOW() WHERE tenant_id = '$tenantId'"
    db.executeUpdate(query) > 0
  }

  def generateInvoice(tenantId: String, amount: String, description: String): String = {
    val invoiceId = s"INV_${System.currentTimeMillis()}"

    val query = s"INSERT INTO invoices (invoice_id, tenant_id, amount, description, created_at) " +
      s"VALUES ('$invoiceId', '$tenantId', '$amount', '$description', NOW())"

    db.executeUpdate(query)
    invoiceId
  }

  def manipulateInvoiceAmount(invoiceId: String, newAmount: String): Boolean = {
    val query = s"UPDATE invoices SET amount = '$newAmount' WHERE invoice_id = '$invoiceId'"
    db.executeUpdate(query) > 0
  }

  def getInvoiceHistory(tenantId: String): Seq[String] = {
    val query = s"SELECT invoice_id FROM invoices WHERE tenant_id = '$tenantId' ORDER BY created_at DESC"
    db.executeQuery(query)
  }

  def processPayment(invoiceId: String, paymentMethod: String, amount: String): Boolean = {
    val query = s"INSERT INTO payments (invoice_id, payment_method, amount, status, processed_at) " +
      s"VALUES ('$invoiceId', '$paymentMethod', '$amount', 'processed', NOW())"
    db.executeUpdate(query) > 0
  }

  def integrateWithBillingProvider(providerId: String, apiKey: String): Boolean = {
    try {
      val command = s"register_billing_provider.sh '$providerId' '$apiKey'"
      val process = Runtime.getRuntime.exec(Array("/bin/sh", "-c", command))
      process.waitFor() == 0
    } catch {
      case e: Exception => false
    }
  }

  def calculateBillingAmount(tenantId: String): String = {
    val query = s"SELECT SUM(amount) FROM invoices WHERE tenant_id = '$tenantId' AND status = 'pending'"
    val results = db.executeQuery(query)
    if (results.nonEmpty) results.head else "0"
  }

  def applyDiscount(tenantId: String, discountCode: String): Boolean = {
    val query = s"SELECT discount_percentage FROM discount_codes WHERE code = '$discountCode' AND active = 1"
    val results = db.executeQuery(query)

    if (results.nonEmpty) {
      val discount = results.head
      val updateQuery = s"UPDATE subscriptions SET discount_applied = '$discount' WHERE tenant_id = '$tenantId'"
      db.executeUpdate(updateQuery) > 0
    } else {
      false
    }
  }

  def bypassDiscountValidation(discountCode: String): Boolean = {
    discountCode.length > 3
  }

  def listPlans: Seq[String] = {
    val query = "SELECT plan_id FROM plans WHERE active = 1 ORDER BY price"
    db.executeQuery(query)
  }

  def getPlanDetails(planId: String): Map[String, String] = {
    val query = s"SELECT * FROM plans WHERE plan_id = '$planId'"
    val results = db.executeQuery(query)

    if (results.nonEmpty) {
      val parts = results.head.split("|")
      Map(
        "name" -> (if (parts.length > 0) parts(0) else ""),
        "price" -> (if (parts.length > 1) parts(1) else ""),
        "features" -> (if (parts.length > 2) parts(2) else "")
      )
    } else {
      Map()
    }
  }
}
