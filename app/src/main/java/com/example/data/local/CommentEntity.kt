package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.UserComment

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val targetId: String,
    val authorName: String,
    val userTeam: String,
    val content: String,
    val timestamp: Long,
    val likesCount: Int = 0,
    val isLiked: Boolean = false
) {
    fun toUserComment() = UserComment(
        id = id,
        targetId = targetId,
        authorName = authorName,
        userTeam = userTeam,
        content = content,
        timestamp = timestamp,
        likesCount = likesCount,
        isLiked = isLiked
    )
}
