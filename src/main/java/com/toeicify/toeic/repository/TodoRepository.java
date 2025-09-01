package com.toeicify.toeic.repository;

import com.toeicify.toeic.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Created by hungpham on 9/1/2025
 */
@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {
    @Modifying
    @Query("UPDATE Todo t SET t.isCompleted = :completed " +
            "WHERE t.todoId = :todoId AND t.schedule.user.userId = :userId")
    int updateCompletionIfOwner(@Param("todoId") Long todoId,
                                @Param("userId") Long userId,
                                @Param("completed") boolean completed);

    @Query("SELECT t.schedule.user.userId FROM Todo t WHERE t.todoId = :todoId")
    Long findOwnerIdByTodoId(@Param("todoId") Long todoId);
}
