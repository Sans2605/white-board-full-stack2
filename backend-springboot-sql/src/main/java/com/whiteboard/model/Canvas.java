package com.whiteboard.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

/**
 * SQL schema:
 *   canvases(id BIGINT PK, owner_id BIGINT FK→users, elements_json TEXT, created_at TIMESTAMP)
 *   canvas_shared(canvas_id BIGINT FK→canvases, user_id BIGINT FK→users)
 *
 * elements_json stores the drawing elements as a JSON string
 * (same data as before, just serialised into a TEXT column).
 */
@Data
@Entity
@Table(name = "canvases")
public class Canvas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user who owns this canvas */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /**
     * Drawing elements stored as a JSON string.
     * We use a TEXT / CLOB column so it can hold large boards.
     */
    @Column(name = "elements_json", columnDefinition = "TEXT")
    private String elementsJson = "[]";

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt = new Date();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = new Date();
    }
}
