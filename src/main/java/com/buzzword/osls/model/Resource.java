package com.buzzword.osls.model;

import com.buzzword.osls.model.enums.ResourceCategory;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="resources")
public class Resource {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=200)
    private String title;

    @Column(columnDefinition="TEXT")
    private String description;

    @Column(nullable=false, length=500)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private ResourceCategory category;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="added_by", nullable=false)
    private User addedBy;

    @Column(name="created_at", nullable=false, updatable=false)
    private LocalDateTime createdAt;

    @Column(name="updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy="resource", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
    private List<Comment> comments=new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt=LocalDateTime.now();
        updatedAt=LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt=LocalDateTime.now();
    }

    public Resource() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id=id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title=title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description=description; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url=url; }
    public ResourceCategory getCategory() { return category; }
    public void setCategory(ResourceCategory category) { this.category=category; }
    public User getAddedBy() { return addedBy; }
    public void setAddedBy(User addedBy) { this.addedBy=addedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<Comment> getComments() { return comments; }
}
