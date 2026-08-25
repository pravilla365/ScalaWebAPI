package com.webapi.services

import scala.xml.XML

class XmlProcessor {

  def parseUserDataXml(xmlContent: String): Map[String, String] = {
    // XXE (XML External Entity) vulnerability - no XXE protection
    val xmlData = XML.loadString(xmlContent)
    val result = scala.collection.mutable.Map[String, String]()

    (xmlData \\ "user").foreach { user =>
      val name = (user \ "name").text
      val email = (user \ "email").text
      result("name") = name
      result("email") = email
    }

    result.toMap
  }

  def parseConfigXml(configXml: String): String = {
    // XXE vulnerability - parsing user-provided XML without protection
    try {
      val config = XML.loadString(configXml)
      (config \ "setting").text
    } catch {
      case e: Exception => s"Error: ${e.getMessage}"
    }
  }
}
