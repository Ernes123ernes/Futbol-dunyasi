package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE targetId = :targetId ORDER BY timestamp DESC")
    fun getCommentsByTarget(targetId: String): Flow<List<CommentEntity>>

    @Query("SELECT * FROM comments ORDER BY timestamp DESC")
    fun getAllComments(): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity): Long

    @Query("UPDATE comments SET likesCount = likesCount + :delta, isLiked = :isLiked WHERE id = :id")
    suspend fun updateLike(id: Long, delta: Int, isLiked: Boolean)

    @Query("DELETE FROM comments WHERE id = :id")
    suspend fun deleteComment(id: Long)
}
