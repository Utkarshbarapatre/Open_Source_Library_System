package com.buzzword.osls.dto;

public class ResourceRequest {
    private String title;
    private String description;
    private String url;
    private String category;

    public ResourceRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title=title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description=description; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url=url; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category=category; }
}
