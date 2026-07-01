package com.buzzword.osls.service;

import com.buzzword.osls.dto.ResourceRequest;
import com.buzzword.osls.dto.ResourceResponse;
import com.buzzword.osls.model.Resource;
import com.buzzword.osls.model.User;
import com.buzzword.osls.model.enums.ResourceCategory;
import com.buzzword.osls.repository.ResourceRepository;
import com.buzzword.osls.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResourceService {

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        String username=SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private ResourceResponse toResponse(Resource r) {
        ResourceResponse resp=new ResourceResponse();
        resp.setId(r.getId());
        resp.setTitle(r.getTitle());
        resp.setDescription(r.getDescription());
        resp.setUrl(r.getUrl());
        resp.setCategory(r.getCategory().name());
        resp.setAddedByUsername(r.getAddedBy().getUsername());
        resp.setAddedById(r.getAddedBy().getId());
        resp.setCreatedAt(r.getCreatedAt());
        resp.setUpdatedAt(r.getUpdatedAt());
        resp.setCommentCount(r.getComments().size());
        return resp;
    }

    private void validateUrl(String url) {
        try {
            new URL(url).toURI();
        } catch (Exception e) {
            throw new RuntimeException("Invalid URL format");
        }
    }

    public List<ResourceResponse> getAllResources() {
        return resourceRepository.findAll().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public ResourceResponse getResourceById(Long id) {
        Resource r=resourceRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Resource not found"));
        return toResponse(r);
    }

    public List<ResourceResponse> searchResources(String query, String category) {
        List<Resource> results;
        if (category!=null && !category.isBlank()) {
            ResourceCategory cat=ResourceCategory.valueOf(category.toUpperCase());
            if (query!=null && !query.isBlank()) {
                results=resourceRepository.searchResourcesByQueryAndCategory(query, cat);
            } else {
                results=resourceRepository.findByCategory(cat);
            }
        } else if (query!=null && !query.isBlank()) {
            results=resourceRepository.searchResources(query);
        } else {
            results=resourceRepository.findAll();
        }
        return results.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public ResourceResponse createResource(ResourceRequest req) {
        validateUrl(req.getUrl());
        User user=getCurrentUser();

        Resource r=new Resource();
        r.setTitle(req.getTitle().trim());
        r.setDescription(req.getDescription());
        r.setUrl(req.getUrl().trim());
        r.setCategory(ResourceCategory.valueOf(req.getCategory().toUpperCase()));
        r.setAddedBy(user);

        return toResponse(resourceRepository.save(r));
    }

    @Transactional
    public ResourceResponse updateResource(Long id, ResourceRequest req) {
        Resource r=resourceRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Resource not found"));

        User currentUser=getCurrentUser();
        boolean isAdmin=currentUser.getRole().name().equals("ADMIN");
        boolean isOwner=r.getAddedBy().getId().equals(currentUser.getId());

        if (!isOwner && !isAdmin) {
            throw new RuntimeException("Access denied");
        }

        validateUrl(req.getUrl());
        r.setTitle(req.getTitle().trim());
        r.setDescription(req.getDescription());
        r.setUrl(req.getUrl().trim());
        r.setCategory(ResourceCategory.valueOf(req.getCategory().toUpperCase()));

        return toResponse(resourceRepository.save(r));
    }

    @Transactional
    public void deleteResource(Long id) {
        Resource r=resourceRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Resource not found"));

        User currentUser=getCurrentUser();
        boolean isAdmin=currentUser.getRole().name().equals("ADMIN");
        boolean isOwner=r.getAddedBy().getId().equals(currentUser.getId());

        if (!isOwner && !isAdmin) {
            throw new RuntimeException("Access denied");
        }

        resourceRepository.delete(r);
    }
}
