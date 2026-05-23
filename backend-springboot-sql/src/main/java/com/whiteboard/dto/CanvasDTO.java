package com.whiteboard.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * API response shape for a canvas — mirrors what the old MongoDB version
 * returned so the React frontend does not need any changes.
 */
@Data
public class CanvasDTO {
    private String id;          // sent as String to match old Mongo ObjectId strings
    private String owner;       // owner user id as String
    private List<String> shared; // list of shared user ids as Strings
    private List<Object> elements;
    private Date createdAt;
}
