package com.buzzword.osls.controller;

import com.buzzword.osls.dto.ApiResponse;
import com.buzzword.osls.dto.CommentRequest;
import com.buzzword.osls.dto.CommentResponse;
import com.buzzword.osls.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/resources/{resourceId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long resourceId) {
        return ResponseEntity.ok(commentService.getCommentsForResource(resourceId));
    }

    @GetMapping("/comments/{commentId}")
    public ResponseEntity<?> getCommentById(@PathVariable Long commentId) {
        try {
            CommentResponse resp=commentService.getCommentById(commentId);
            return ResponseEntity.ok(resp);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/resources/{resourceId}/comments")
    public ResponseEntity<?> addComment(@PathVariable Long resourceId,
                                        @RequestBody CommentRequest req) {
        try {
            CommentResponse resp=commentService.addComment(resourceId, req);
            return ResponseEntity.ok(resp);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<?> updateComment(@PathVariable Long commentId,
                                           @RequestBody CommentRequest req) {
        try {
            CommentResponse resp=commentService.updateComment(commentId, req);
            return ResponseEntity.ok(resp);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId) {
        try {
            commentService.deleteComment(commentId);
            return ResponseEntity.ok(new ApiResponse(true, "Comment deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        }
    }
}
