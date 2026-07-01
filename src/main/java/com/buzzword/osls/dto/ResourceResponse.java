package com.buzzword.osls.dto;

import java.time.LocalDateTime;

public class ResourceResponse {
    private Long id;
    private String title;
    private String description;
    private String url;
    private String category;
    private String addedByUsername;
    private Long addedById;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int commentCount;

    public ResourceResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id=id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title=title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description=description; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url=url; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category=category; }
    public String getAddedByUsername() { return addedByUsername; }
    public void setAddedByUsername(String addedByUsername) { this.addedByUsername=addedByUsername; }
    public Long getAddedById() { return addedById; }
    public void setAddedById(Long addedById) { this.addedById=addedById; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt=createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt=updatedAt; }
    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount=commentCount; }
}
