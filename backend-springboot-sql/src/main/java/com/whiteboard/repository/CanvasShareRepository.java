package com.whiteboard.repository;

import com.whiteboard.model.Canvas;
import com.whiteboard.model.CanvasShare;
import com.whiteboard.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CanvasShareRepository extends JpaRepository<CanvasShare, Long> {
    List<CanvasShare> findByCanvas(Canvas canvas);
    Optional<CanvasShare> findByCanvasAndUser(Canvas canvas, User user);
    boolean existsByCanvasAndUser(Canvas canvas, User user);
    void deleteByCanvasAndUser(Canvas canvas, User user);
}
