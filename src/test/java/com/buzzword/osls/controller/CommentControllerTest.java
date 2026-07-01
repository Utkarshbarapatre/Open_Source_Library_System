package com.buzzword.osls.controller;

import com.buzzword.osls.dto.ApiResponse;
import com.buzzword.osls.dto.CommentRequest;
import com.buzzword.osls.dto.CommentResponse;
import com.buzzword.osls.service.CommentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentControllerTest {

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentController commentController;

    @Test
    public void testGetComments() {
        CommentResponse resp = new CommentResponse();
        resp.setId(1L);
        resp.setContent("Good article");

        when(commentService.getCommentsForResource(10L)).thenReturn(Collections.singletonList(resp));

        ResponseEntity<List<CommentResponse>> response = commentController.getComments(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Good article", response.getBody().get(0).getContent());
    }

    @Test
    public void testGetCommentById_Success() {
        CommentResponse resp = new CommentResponse();
        resp.setId(1L);

        when(commentService.getCommentById(1L)).thenReturn(resp);

        ResponseEntity<?> response = commentController.getCommentById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(resp, response.getBody());
    }

    @Test
    public void testGetCommentById_NotFound() {
        when(commentService.getCommentById(1L)).thenThrow(new RuntimeException("Not found"));

        ResponseEntity<?> response = commentController.getCommentById(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void testAddComment_Success() {
        CommentRequest req = new CommentRequest();
        CommentResponse resp = new CommentResponse();
        resp.setId(100L);

        when(commentService.addComment(eq(10L), any(CommentRequest.class))).thenReturn(resp);

        ResponseEntity<?> response = commentController.addComment(10L, req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(resp, response.getBody());
    }

    @Test
    public void testAddComment_Failure() {
        CommentRequest req = new CommentRequest();

        when(commentService.addComment(eq(10L), any(CommentRequest.class))).thenThrow(new RuntimeException("Resource not found"));

        ResponseEntity<?> response = commentController.addComment(10L, req);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse body = (ApiResponse) response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals("Resource not found", body.getMessage());
    }

    @Test
    public void testUpdateComment_Success() {
        CommentRequest req = new CommentRequest();
        CommentResponse resp = new CommentResponse();
        resp.setId(1L);

        when(commentService.updateComment(eq(1L), any(CommentRequest.class))).thenReturn(resp);

        ResponseEntity<?> response = commentController.updateComment(1L, req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(resp, response.getBody());
    }

    @Test
    public void testUpdateComment_Failure() {
        CommentRequest req = new CommentRequest();

        when(commentService.updateComment(eq(1L), any(CommentRequest.class))).thenThrow(new RuntimeException("Access denied"));

        ResponseEntity<?> response = commentController.updateComment(1L, req);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse body = (ApiResponse) response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals("Access denied", body.getMessage());
    }

    @Test
    public void testDeleteComment_Success() {
        doNothing().when(commentService).deleteComment(1L);

        ResponseEntity<?> response = commentController.deleteComment(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse body = (ApiResponse) response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
        assertEquals("Comment deleted", body.getMessage());
    }

    @Test
    public void testDeleteComment_Failure() {
        doThrow(new RuntimeException("Access denied")).when(commentService).deleteComment(1L);

        ResponseEntity<?> response = commentController.deleteComment(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse body = (ApiResponse) response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals("Access denied", body.getMessage());
    }
}
