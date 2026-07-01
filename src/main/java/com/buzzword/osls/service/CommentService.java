package com.buzzword.osls.service;

import com.buzzword.osls.dto.CommentRequest;
import com.buzzword.osls.dto.CommentResponse;
import com.buzzword.osls.model.Comment;
import com.buzzword.osls.model.Resource;
import com.buzzword.osls.model.User;
import com.buzzword.osls.repository.CommentRepository;
import com.buzzword.osls.repository.ResourceRepository;
import com.buzzword.osls.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        String username=SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private CommentResponse toResponse(Comment c) {
        CommentResponse resp=new CommentResponse();
        resp.setId(c.getId());
        resp.setContent(c.getContent());
        resp.setUsername(c.getUser().getUsername());
        resp.setUserId(c.getUser().getId());
        resp.setResourceId(c.getResource().getId());
        resp.setCreatedAt(c.getCreatedAt());
        resp.setUpdatedAt(c.getUpdatedAt());
        return resp;
    }

    public List<CommentResponse> getCommentsForResource(Long resourceId) {
        return commentRepository.findByResourceId(resourceId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public CommentResponse getCommentById(Long commentId) {
        Comment comment=commentRepository.findById(commentId)
            .orElseThrow(() -> new RuntimeException("Comment not found"));
        return toResponse(comment);
    }

    @Transactional
    public CommentResponse addComment(Long resourceId, CommentRequest req) {
        Resource resource=resourceRepository.findById(resourceId)
            .orElseThrow(() -> new RuntimeException("Resource not found"));
        User user=getCurrentUser();

        Comment comment=new Comment();
        comment.setContent(req.getContent().trim());
        comment.setResource(resource);
        comment.setUser(user);

        return toResponse(commentRepository.save(comment));
    }

    @Transactional
    public CommentResponse updateComment(Long commentId, CommentRequest req) {
        Comment comment=commentRepository.findById(commentId)
            .orElseThrow(() -> new RuntimeException("Comment not found"));

        User currentUser=getCurrentUser();
        boolean isOwner=comment.getUser().getId().equals(currentUser.getId());

        // Editing is owner-only. Admins can delete any comment (see deleteComment),
        // but may not edit someone else's comment content.
        if (!isOwner) {
            throw new RuntimeException("Access denied: only the comment author can edit this comment");
        }

        comment.setContent(req.getContent().trim());
        return toResponse(commentRepository.save(comment));
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment=commentRepository.findById(commentId)
            .orElseThrow(() -> new RuntimeException("Comment not found"));

        User currentUser=getCurrentUser();
        boolean isAdmin=currentUser.getRole().name().equals("ADMIN");
        boolean isOwner=comment.getUser().getId().equals(currentUser.getId());

        if (!isOwner && !isAdmin) {
            throw new RuntimeException("Access denied");
        }

        commentRepository.delete(comment);
    }
}
