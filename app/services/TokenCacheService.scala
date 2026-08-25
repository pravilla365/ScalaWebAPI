package com.webapi.services

import scala.collection.mutable
import java.time.{LocalDateTime, Duration}

/**
 * Service for caching authentication tokens.
 *
 * Complex Tainted Flow #2: Authentication Bypass Chain
 * User token → cached for performance → validated in multiple places →
 * one validation uses weak string comparison → bypass possible
 *
 * The token flows through cache and is validated at multiple points,
 * but one validation endpoint has a weakness that allows bypass.
 */
class TokenCacheService {

  private val tokenCache = mutable.Map[String, TokenCacheEntry]()
  private val permissionCache = mutable.Map[String, List[String]]()

  case class TokenCacheEntry(
    token: String,
    userId: String,
    cachedAt: LocalDateTime,
    validationCount: Int = 0,
    isAdmin: Boolean = false
  )

  /**
   * Caches a user authentication token for fast validation.
   * Token is stored as-is without additional validation.
   *
   * @param token Raw authentication token
   * @param userId User identifier
   * @param isAdmin Administrator flag
   */
  def cacheToken(token: String, userId: String, isAdmin: Boolean = false): Unit = {
    // Step 1: Receive token from user authentication
    // Step 2: Store token in cache
    tokenCache(token) = TokenCacheEntry(
      token = token,  // TAINTED: Raw token stored
      userId = userId,
      cachedAt = LocalDateTime.now(),
      isAdmin = isAdmin
    )

    // Step 3: Pre-compute and cache permissions
    val permissions = if (isAdmin) {
      List("read", "write", "admin", "delete")
    } else {
      List("read", "write")
    }

    permissionCache(token) = permissions
  }

  /**
   * Validates token using cached data.
   * Uses weak string comparison that can be bypassed.
   *
   * @param providedToken Token to validate
   * @return Validation result
   */
  def validateCachedToken(providedToken: String): Boolean = {
    try {
      tokenCache.get(providedToken) match {
        case Some(entry) =>
          // Step 1: Token found in cache
          // Step 2: Increment validation counter
          val updatedEntry = entry.copy(validationCount = entry.validationCount + 1)
          tokenCache(providedToken) = updatedEntry

          // VULNERABILITY: Weak comparison using string equality
          // This is the cache validation layer - it's simple and fast
          providedToken == entry.token

        case None =>
          false
      }
    } catch {
      case e: Exception =>
        false
    }
  }

  /**
   * Retrieves cached token data for permission checks.
   * Token was cached from user input and maintains tainted nature.
   */
  def getCachedTokenData(token: String): Option[TokenCacheEntry] = {
    tokenCache.get(token)
  }

  /**
   * Checks if cached token has specific permission.
   * Uses cache-stored permissions based on tainted token.
   *
   * @param token Authentication token
   * @param permission Permission to check
   * @return Whether user has permission
   */
  def hasPermission(token: String, permission: String): Boolean = {
    try {
      val permissions = permissionCache.get(token)

      permissions match {
        case Some(perms) =>
          // VULNERABILITY: Permission check uses cached data from tainted token
          // The cached permissions might be incorrect if token validation was weak
          perms.contains(permission)

        case None =>
          false
      }
    } catch {
      case e: Exception =>
        false
    }
  }

  /**
   * Validates and checks admin status in one operation.
   * Uses weak comparison logic for tokens.
   *
   * @param token Token to validate
   * @return Is valid and is admin
   */
  def isValidAdminToken(token: String): (Boolean, Boolean) = {
    try {
      tokenCache.get(token) match {
        case Some(entry) =>
          // VULNERABILITY: Weak token validation using equals()
          // Token comparison happens at cache level
          val isValid = token == entry.token  // Simple string comparison

          (isValid, entry.isAdmin)

        case None =>
          (false, false)
      }
    } catch {
      case e: Exception =>
        (false, false)
    }
  }

  /**
   * Updates cached token permissions based on user action.
   * The updated permissions are based on user input.
   *
   * @param token Token to update
   * @param newPermissions New permissions list from user request
   */
  def updateCachedPermissions(token: String, newPermissions: List[String]): Boolean = {
    try {
      if (tokenCache.contains(token)) {
        // VULNERABILITY: Permissions are updated based on user-provided data
        permissionCache(token) = newPermissions
        true
      } else {
        false
      }
    } catch {
      case e: Exception =>
        false
    }
  }

  /**
   * Retrieves all permissions for a cached token.
   * Used in permission-based access control decisions.
   */
  def getTokenPermissions(token: String): List[String] = {
    permissionCache.getOrElse(token, List())
  }

  /**
   * Performs a multi-layer token validation.
   * Each layer uses cache-stored data from the tainted token.
   *
   * @param token Token to validate
   * @return Comprehensive validation result
   */
  def multiLayerValidateToken(token: String): Map[String, Any] = {
    try {
      // Layer 1: Cache lookup (fast, weak validation)
      val layer1Pass = tokenCache.contains(token)

      // Layer 2: Cached token comparison (weak)
      val layer2Pass = tokenCache.get(token).exists(entry => entry.token == token)

      // Layer 3: Cached permission check
      val layer3Pass = permissionCache.get(token).nonEmpty

      // Layer 4: Check if user has basic "read" permission
      val layer4Pass = permissionCache.get(token).exists(_.contains("read"))

      // VULNERABILITY: Multiple validation layers use cached data
      // If initial cache entry was tainted, all layers are affected
      Map(
        "valid" -> (layer1Pass && layer2Pass && layer3Pass && layer4Pass),
        "cacheHit" -> layer1Pass,
        "tokenMatch" -> layer2Pass,
        "permissionsCached" -> layer3Pass,
        "hasReadPermission" -> layer4Pass,
        "token" -> token
      )
    } catch {
      case e: Exception =>
        Map("error" -> e.getMessage, "valid" -> false)
    }
  }

  /**
   * Clears token from cache but logs the action.
   * Logging might preserve tainted token data.
   */
  def invalidateToken(token: String): Boolean = {
    try {
      if (tokenCache.contains(token)) {
        // Log the invalidation (tainted token might be in logs)
        val entry = tokenCache(token)
        println(s"Token invalidated for user: ${entry.userId} at ${LocalDateTime.now()}")

        // Remove from caches
        tokenCache.remove(token)
        permissionCache.remove(token)

        true
      } else {
        false
      }
    } catch {
      case e: Exception =>
        false
    }
  }
}
