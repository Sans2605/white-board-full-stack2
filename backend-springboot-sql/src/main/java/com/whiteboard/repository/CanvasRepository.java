package com.whiteboard.repository;

import com.whiteboard.model.Canvas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CanvasRepository extends JpaRepository<Canvas, Long> {

    /**
     * Returns all canvases where the given user is either the owner
     * OR appears in the canvas_shared join table.
     */
    @Query("SELECT DISTINCT c FROM Canvas c LEFT JOIN CanvasShare cs ON cs.canvas = c " +
           "WHERE c.owner.id = :userId OR cs.user.id = :userId " +
           "ORDER BY c.createdAt DESC")
    List<Canvas> findAllForUser(@Param("userId") Long userId);
}
