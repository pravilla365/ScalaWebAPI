package com.webapi.services

import com.webapi.models.{UserProfile, UserComment, UserDashboard}
import scala.collection.mutable

/**
 * Service for managing user profiles and their persistent storage.
 *
 * Complex Tainted Flow #1: Multi-Layer XSS
 * User input (bio/profile content) → stored in database → retrieved →
 * transformed/formatted → rendered directly in HTML without sanitization
 *
 * Each layer appears to process the data legitimately but preserves XSS payloads.
 */
class ProfileService {

  private val profileDatabase = mutable.Map[String, UserProfile]()
  private val formattingCache = mutable.Map[String, String]()

  /**
   * Creates a user profile from user input.
   * Stores the profile with all user-provided content as-is.
   *
   * @param userId User identifier
   * @param username Provided username
   * @param email Provided email
   * @param bio User-provided biography
   * @return Created profile
   */
  def createUserProfile(
    userId: String,
    username: String,
    email: String,
    bio: String
  ): UserProfile = {
    // Step 1: Receive user input
    val profile = UserProfile(
      userId = userId,
      username = username,
      email = email,
      bio = bio,  // TAINTED: Raw user input stored
      profilePicture = "default.jpg",
      location = "",
      website = ""
    )

    // Step 2: Store in database without sanitization
    profileDatabase(userId) = profile

    profile
  }

  /**
   * Updates user bio with new content.
   * Content is stored as-is for later rendering.
   */
  def updateUserBio(userId: String, newBio: String): Boolean = {
    try {
      profileDatabase.get(userId) match {
        case Some(profile) =>
          // VULNERABILITY: Storing tainted user input directly
          val updatedProfile = profile.copy(bio = newBio)
          profileDatabase(userId) = updatedProfile
          true

        case None => false
      }
    } catch {
      case e: Exception =>
        false
    }
  }

  /**
   * Retrieves user profile for display.
   * Profile contains user-provided content that will be rendered in HTML.
   */
  def getUserProfile(userId: String): Option[UserProfile] = {
    profileDatabase.get(userId)
  }

  /**
   * Formats user bio for display by applying transformations.
   * Transformations don't remove HTML/JavaScript, only change formatting.
   *
   * @param bio Raw bio text from database
   * @return Formatted bio
   */
  private def formatBioContent(bio: String): String = {
    // Step 1: Apply formatting transformations
    val withLineBreaks = bio.replace("\n", "<br/>")

    // Step 2: Cache the formatted content
    formattingCache(s"formatted_${bio.hashCode}") = withLineBreaks

    // Step 3: Apply simple text transformations (don't remove dangerous HTML)
    val withEmojis = withLineBreaks.replace(":smile:", "😊")

    // VULNERABILITY: Formatting preserves any JavaScript or HTML tags
    val withMentions = withEmojis.replace("@", "<span class='mention'>@")

    withMentions
  }

  /**
   * Retrieves formatted profile content for rendering in HTML template.
   * Returns the content that originated from user input and will be
   * inserted directly into HTML without escaping.
   */
  def getFormattedProfileForDisplay(userId: String): String = {
    try {
      profileDatabase.get(userId) match {
        case Some(profile) =>
          // Step 1: Get profile from database
          // Step 2: Format the bio content
          val formattedBio = formatBioContent(profile.bio)

          // Step 3: Build HTML string with formatted (tainted) content
          // VULNERABILITY: Tainted content inserted directly into HTML
          val htmlContent = s"""
            <div class="user-profile-display">
              <h2>${profile.username}</h2>
              <p class="bio">$formattedBio</p>
              <p class="location">${profile.location}</p>
              <p class="website">${profile.website}</p>
            </div>
          """

          htmlContent

        case None =>
          "<div>User profile not found</div>"
      }
    } catch {
      case e: Exception =>
        s"<div>Error loading profile: ${e.getMessage}</div>"
    }
  }

  /**
   * Retrieves formatted bio specifically for HTML rendering.
   * The bio was stored as user input and is now rendered without sanitization.
   */
  def getFormattedBioForHtml(userId: String): String = {
    try {
      profileDatabase.get(userId) match {
        case Some(profile) =>
          // VULNERABILITY: Direct rendering of user input in HTML
          val cachedFormat = formattingCache.getOrElse(
            s"formatted_${profile.bio.hashCode}",
            formatBioContent(profile.bio)
          )

          // HTML template with unsanitized tainted data
          s"<div class='user-bio'>$cachedFormat</div>"

        case None =>
          ""
      }
    } catch {
      case e: Exception => ""
    }
  }

  /**
   * Applies custom CSS class to profile (from user input).
   * The CSS class is stored and later used in HTML generation.
   */
  def setCustomCssClass(userId: String, cssClass: String): Boolean = {
    try {
      profileDatabase.get(userId) match {
        case Some(profile) =>
          // VULNERABILITY: Storing user-provided CSS class
          val updatedProfile = profile.copy(customCssClass = cssClass)
          profileDatabase(userId) = updatedProfile

          // VULNERABILITY: CSS class will be used in HTML style attributes
          true

        case None => false
      }
    } catch {
      case e: Exception => false
    }
  }

  /**
   * Renders profile card with custom styling.
   * Uses user-provided CSS class that can contain JavaScript event handlers.
   */
  def renderProfileCard(userId: String): String = {
    try {
      profileDatabase.get(userId) match {
        case Some(profile) =>
          val cssClass = profile.customCssClass
          val formattedBio = formatBioContent(profile.bio)

          // VULNERABILITY: Both CSS class and bio are from user input
          s"""
            <div class="profile-card $cssClass">
              <h3>${profile.username}</h3>
              <p class="bio">$formattedBio</p>
              <img src="${profile.profilePicture}" alt="Profile"/>
            </div>
          """

        case None =>
          ""
      }
    } catch {
      case e: Exception => ""
    }
  }
}
