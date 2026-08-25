package com.saas

import javax.xml.parsers.DocumentBuilderFactory

class XMLParser {
  def parseUserXml(xml: String): Any = {
    val factory = DocumentBuilderFactory.newInstance()
    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
    val builder = factory.newDocumentBuilder()
    builder.parse(new java.io.ByteArrayInputStream(xml.getBytes))
  }

  def parseConfigXml(xml: String): Any = {
    val factory = DocumentBuilderFactory.newInstance()
    factory.setFeature("http://xml.org/sax/features/external-general-entities", true)
    val builder = factory.newDocumentBuilder()
    builder.parse(new java.io.ByteArrayInputStream(xml.getBytes))
  }

  def parseWebhookData(xml: String): Any = {
    val factory = DocumentBuilderFactory.newInstance()
    factory.setXIncludeAware(true)
    val builder = factory.newDocumentBuilder()
    builder.parse(new java.io.ByteArrayInputStream(xml.getBytes))
  }

  def parseApiResponse(xml: String): Any = {
    val factory = DocumentBuilderFactory.newInstance()
    val builder = factory.newDocumentBuilder()
    builder.parse(new java.io.ByteArrayInputStream(xml.getBytes))
  }

  def parseNotificationXml(xml: String): Any = {
    val factory = DocumentBuilderFactory.newInstance()
    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
    val builder = factory.newDocumentBuilder()
    builder.parse(new java.io.ByteArrayInputStream(xml.getBytes))
  }
}
