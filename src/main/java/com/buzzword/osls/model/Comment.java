package com.buzzword.osls.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="comments")
public class Comment {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, columnDefinition="TEXT")
    private String content;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="resource_id", nullable=false)
    private Resource resource;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false)
    private User user;

    @Column(name="created_at", nullable=false, updatable=false)
    private LocalDateTime createdAt;

    @Column(name="updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt=LocalDateTime.now();
        updatedAt=LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt=LocalDateTime.now();
    }

    public Comment() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id=id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content=content; }
    public Resource getResource() { return resource; }
    public void setResource(Resource resource) { this.resource=resource; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user=user; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
