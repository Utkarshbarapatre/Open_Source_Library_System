package com.buzzword.osls.controller;

import com.buzzword.osls.dto.*;
import com.buzzword.osls.service.*;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private ResourceService resourceService;

    @Mock
    private CommentService commentService;

    @InjectMocks
    private AdminController adminController;

    @Test
    public void testGetAllUsers() {
        UserResponse resp = new UserResponse(1L, "user1", "user1@test.com", "USER", null);

        when(userService.getAllUsers()).thenReturn(Collections.singletonList(resp));

        ResponseEntity<List<UserResponse>> response = adminController.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("user1", response.getBody().get(0).getUsername());
    }

    @Test
    public void testGetUserById_Success() {
        UserResponse resp = new UserResponse(1L, "user1", "user1@test.com", "USER", null);

        when(userService.getUserResponseById(1L)).thenReturn(resp);

        ResponseEntity<?> response = adminController.getUserById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(resp, response.getBody());
    }

    @Test
    public void testGetUserById_NotFound() {
        when(userService.getUserResponseById(1L)).thenThrow(new RuntimeException("Not found"));

        ResponseEntity<?> response = adminController.getUserById(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void testDeleteUser_Success() {
        doNothing().when(userService).deleteUser(1L);

        ResponseEntity<?> response = adminController.deleteUser(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse body = (ApiResponse) response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
        assertEquals("User deleted", body.getMessage());
    }

    @Test
    public void testDeleteUser_Failure() {
        doThrow(new RuntimeException("User not found")).when(userService).deleteUser(1L);

        ResponseEntity<?> response = adminController.deleteUser(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse body = (ApiResponse) response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals("User not found", body.getMessage());
    }

    @Test
    public void testGetResourceById_Success() {
        ResourceResponse resp = new ResourceResponse();
        resp.setId(10L);
        resp.setTitle("Resource Title");

        when(resourceService.getResourceById(10L)).thenReturn(resp);

        ResponseEntity<?> response = adminController.getResourceById(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(resp, response.getBody());
    }

    @Test
    public void testGetResourceById_NotFound() {
        when(resourceService.getResourceById(10L)).thenThrow(new RuntimeException("Not found"));

        ResponseEntity<?> response = adminController.getResourceById(10L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void testDeleteResource_Success() {
        doNothing().when(resourceService).deleteResource(10L);

        ResponseEntity<?> response = adminController.deleteResource(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse body = (ApiResponse) response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
        assertEquals("Resource deleted", body.getMessage());
    }

    @Test
    public void testDeleteResource_Failure() {
        doThrow(new RuntimeException("Resource not found")).when(resourceService).deleteResource(10L);

        ResponseEntity<?> response = adminController.deleteResource(10L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse body = (ApiResponse) response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals("Resource not found", body.getMessage());
    }

    @Test
    public void testGetCommentById_Success() {
        CommentResponse resp = new CommentResponse();
        resp.setId(100L);
        resp.setContent("Comment content");

        when(commentService.getCommentById(100L)).thenReturn(resp);

        ResponseEntity<?> response = adminController.getCommentById(100L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(resp, response.getBody());
    }

    @Test
    public void testGetCommentById_NotFound() {
        when(commentService.getCommentById(100L)).thenThrow(new RuntimeException("Not found"));

        ResponseEntity<?> response = adminController.getCommentById(100L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void testDeleteComment_Success() {
        doNothing().when(commentService).deleteComment(100L);

        ResponseEntity<?> response = adminController.deleteComment(100L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse body = (ApiResponse) response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
        assertEquals("Comment deleted", body.getMessage());
    }

    @Test
    public void testDeleteComment_Failure() {
        doThrow(new RuntimeException("Comment not found")).when(commentService).deleteComment(100L);

        ResponseEntity<?> response = adminController.deleteComment(100L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse body = (ApiResponse) response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals("Comment not found", body.getMessage());
    }
}
