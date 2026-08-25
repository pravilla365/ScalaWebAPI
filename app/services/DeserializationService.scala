package com.webapi.services

import com.webapi.models.User
import java.io.ByteArrayInputStream
import java.util.Base64
import scala.collection.mutable

/**
 * Service for deserializing user data from various formats.
 *
 * Complex Tainted Flow #3: Indirect Deserialization
 * User parameter (type) → lookup in deserializer map → pass untrusted data
 * to deserializer → unsafe deserialization with gadget chain potential
 *
 * The type parameter is used to select a deserializer, then untrusted
 * data is passed to that deserializer without additional validation.
 */
class DeserializationService {

  // Map of deserializer types to their implementation
  private val deserializerMap = mutable.Map[String, UserDeserializer]()

  // Initialize available deserializers
  initializeDeserializers()

  private def initializeDeserializers(): Unit = {
    deserializerMap("default") = new DefaultUserDeserializer()
    deserializerMap("json") = new JsonUserDeserializer()
    deserializerMap("binary") = new BinaryUserDeserializer()
    deserializerMap("xml") = new XmlUserDeserializer()
    deserializerMap("custom") = new CustomUserDeserializer()
  }

  /**
   * Deserializes user data based on type parameter.
   * Type comes from user input and is used to select deserializer.
   *
   * @param deserializationType Type of deserializer to use
   * @param encodedData Encoded user data to deserialize
   * @return Deserialized User object
   */
  def deserializeUserData(deserializationType: String, encodedData: String): User = {
    try {
      // Step 1: User provides deserialization type
      // Step 2: Lookup deserializer from map based on user input
      val deserializer = deserializerMap.get(deserializationType)

      deserializer match {
        case Some(d) =>
          // Step 3: Pass untrusted data to selected deserializer
          // VULNERABILITY: User-selected deserializer processes untrusted data
          d.deserialize(encodedData)

        case None =>
          // Step 4: If type not found, use default deserializer
          // VULNERABILITY: Even default deserializer is unsafe
          val defaultDeserializer = deserializerMap("default")
          defaultDeserializer.deserialize(encodedData)
      }
    } catch {
      case e: Exception =>
        User("", "error", "error@example.com", "")
    }
  }

  /**
   * Registers a custom deserializer type.
   * Allows users to add new deserialization strategies.
   */
  def registerDeserializer(typeName: String, deserializer: UserDeserializer): Unit = {
    // VULNERABILITY: User can register custom deserializers
    // These could be malicious implementations
    deserializerMap(typeName) = deserializer
  }

  /**
   * Retrieves available deserializer types.
   * List might be used by attackers to select specific gadget chains.
   */
  def getAvailableDeserializers: List[String] = {
    deserializerMap.keys.toList
  }

  /**
   * Processes user data with chained deserializers.
   * Each step uses a deserializer selected by user.
   */
  def chainedDeserialization(
    deserializationChain: List[String],
    initialData: String
  ): User = {
    try {
      var currentData = initialData

      for (deserializerType <- deserializationChain) {
        // VULNERABILITY: Each step uses user-selected deserializer
        val deserializer = deserializerMap.getOrElse(
          deserializerType,
          deserializerMap("default")
        )

        // Process data through each deserializer in chain
        // VULNERABILITY: Untrusted data flows through multiple stages
        val intermediateUser = deserializer.deserialize(currentData)

        // Re-encode for next stage (maintains tainted data)
        currentData = Base64.getEncoder.encodeToString(
          intermediateUser.email.getBytes()
        )
      }

      // Final deserialization
      val finalDeserializer = deserializerMap.getOrElse(
        deserializationChain.lastOption.getOrElse("default"),
        deserializerMap("default")
      )

      finalDeserializer.deserialize(currentData)
    } catch {
      case e: Exception =>
        User("", "error", "error@example.com", "")
    }
  }

  /**
   * Deserializes user based on content type parameter.
   * Content type comes from user and selects deserialization strategy.
   */
  def deserializeByContentType(contentType: String, data: String): User = {
    // VULNERABILITY: Content type parameter selects deserializer
    deserializeUserData(contentType, data)
  }
}

/**
 * Base trait for user deserializers.
 */
trait UserDeserializer {
  def deserialize(data: String): User
}

/**
 * Default deserializer using Java ObjectInputStream.
 * Vulnerable to gadget chain attacks.
 */
class DefaultUserDeserializer extends UserDeserializer {
  override def deserialize(data: String): User = {
    try {
      val decoder = Base64.getDecoder
      val decodedBytes = decoder.decode(data)

      // VULNERABILITY: Unsafe Java deserialization
      val ois = new java.io.ObjectInputStream(new ByteArrayInputStream(decodedBytes))
      val obj = ois.readObject()
      ois.close()

      obj.asInstanceOf[User]
    } catch {
      case e: Exception =>
        User("", "default", "default@example.com", "")
    }
  }
}

/**
 * JSON-based deserializer.
 * Parses JSON and constructs User object.
 */
class JsonUserDeserializer extends UserDeserializer {
  override def deserialize(data: String): User = {
    try {
      // VULNERABILITY: No validation of JSON structure or content
      val decoder = Base64.getDecoder
      val jsonString = new String(decoder.decode(data))

      // Simple JSON parsing without sanitization
      // Could be exploited with malicious JSON payloads
      User(
        id = extractJsonField(jsonString, "id"),
        username = extractJsonField(jsonString, "username"),
        email = extractJsonField(jsonString, "email"),
        passwordHash = extractJsonField(jsonString, "password")
      )
    } catch {
      case e: Exception =>
        User("", "json", "json@example.com", "")
    }
  }

  private def extractJsonField(json: String, field: String): String = {
    val pattern = s""""$field"\\s*:\\s*"([^"]*)".""".r
    pattern.findFirstMatchIn(json).map(_.group(1)).getOrElse("")
  }
}

/**
 * Binary format deserializer.
 * Processes binary serialized data.
 */
class BinaryUserDeserializer extends UserDeserializer {
  override def deserialize(data: String): User = {
    try {
      val decoder = Base64.getDecoder
      val bytes = decoder.decode(data)

      // VULNERABILITY: Direct byte parsing without validation
      val id = new String(bytes.slice(0, 8))
      val username = new String(bytes.slice(8, 20))
      val email = new String(bytes.slice(20, 50))
      val password = new String(bytes.slice(50, 100))

      User(id, username, email, password)
    } catch {
      case e: Exception =>
        User("", "binary", "binary@example.com", "")
    }
  }
}

/**
 * XML-based deserializer.
 * Vulnerable to XXE attacks.
 */
class XmlUserDeserializer extends UserDeserializer {
  override def deserialize(data: String): User = {
    try {
      val decoder = Base64.getDecoder
      val xmlString = new String(decoder.decode(data))

      // VULNERABILITY: XXE vulnerability - parsing untrusted XML
      val xml = scala.xml.XML.loadString(xmlString)

      User(
        id = (xml \\ "id").text,
        username = (xml \\ "username").text,
        email = (xml \\ "email").text,
        passwordHash = (xml \\ "password").text
      )
    } catch {
      case e: Exception =>
        User("", "xml", "xml@example.com", "")
    }
  }
}

/**
 * Custom user deserializer for experimental formats.
 * Allows arbitrary deserialization logic.
 */
class CustomUserDeserializer extends UserDeserializer {
  override def deserialize(data: String): User = {
    try {
      val decoder = Base64.getDecoder
      val customData = new String(decoder.decode(data))

      // VULNERABILITY: Arbitrary parsing without validation
      // Could be exploited with carefully crafted payloads
      val parts = customData.split("|")

      User(
        id = if (parts.length > 0) parts(0) else "",
        username = if (parts.length > 1) parts(1) else "",
        email = if (parts.length > 2) parts(2) else "",
        passwordHash = if (parts.length > 3) parts(3) else ""
      )
    } catch {
      case e: Exception =>
        User("", "custom", "custom@example.com", "")
    }
  }
}
