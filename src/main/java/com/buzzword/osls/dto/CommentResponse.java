package com.buzzword.osls.dto;

import java.time.LocalDateTime;

public class CommentResponse {
    private Long id;
    private String content;
    private String username;
    private Long userId;
    private Long resourceId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CommentResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id=id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content=content; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username=username; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId=userId; }
    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId=resourceId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt=createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt=updatedAt; }
}
