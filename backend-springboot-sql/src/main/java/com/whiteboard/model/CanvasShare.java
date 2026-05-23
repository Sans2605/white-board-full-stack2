package com.whiteboard.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the many-to-many relationship between a Canvas and the Users
 * it has been shared with.  Stored in the canvas_shared table.
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "canvas_shared",
       uniqueConstraints = @UniqueConstraint(columnNames = {"canvas_id", "user_id"}))
public class CanvasShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "canvas_id", nullable = false)
    private Canvas canvas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public CanvasShare(Canvas canvas, User user) {
        this.canvas = canvas;
        this.user = user;
    }
}
