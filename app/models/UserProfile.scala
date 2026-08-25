package com.webapi.models

/**
 * User profile model with database persistence capabilities.
 * Stores user-provided content that will later be rendered in HTML.
 */
case class UserProfile(
  userId: String,
  username: String,
  email: String,
  bio: String,
  profilePicture: String,
  location: String,
  website: String,
  followersCount: Int = 0,
  followingCount: Int = 0,
  joinDate: String = java.time.LocalDate.now().toString,
  isVerified: Boolean = false,
  profileSummary: String = "",
  customCssClass: String = ""
)

/**
 * Model for storing user activity and comments.
 * Content is stored as-is and later rendered without sanitization.
 */
case class UserComment(
  commentId: String,
  userId: String,
  targetUserId: String,
  commentText: String,
  commentedAt: String,
  likeCount: Int = 0,
  replyCount: Int = 0,
  hashtags: List[String] = List(),
  mentions: List[String] = List(),
  mediaLinks: List[String] = List()
)

/**
 * Model for user dashboard data.
 * Aggregates user information that will be displayed in HTML without sanitization.
 */
case class UserDashboard(
  userId: String,
  userProfile: UserProfile,
  recentComments: List[UserComment],
  recentPosts: List[String],
  suggestedUsers: List[String],
  notificationCount: Int,
  renderableContent: String = ""
)
