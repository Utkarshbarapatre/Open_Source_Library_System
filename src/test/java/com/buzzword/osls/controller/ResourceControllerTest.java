package com.buzzword.osls.controller;

import com.buzzword.osls.dto.*;
import com.buzzword.osls.service.ResourceService;
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
public class ResourceControllerTest {

    @Mock
    private ResourceService resourceService;

    @InjectMocks
    private ResourceController resourceController;

    @Test
    public void testGetAllResources() {
        ResourceResponse resp = new ResourceResponse();
        resp.setId(1L);
        resp.setTitle("Resource Title");

        when(resourceService.getAllResources()).thenReturn(Collections.singletonList(resp));

        ResponseEntity<List<ResourceResponse>> response = resourceController.getAllResources();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Resource Title", response.getBody().get(0).getTitle());
    }

    @Test
    public void testGetResourceById_Success() {
        ResourceResponse resp = new ResourceResponse();
        resp.setId(1L);

        when(resourceService.getResourceById(1L)).thenReturn(resp);

        ResponseEntity<?> response = resourceController.getResourceById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(resp, response.getBody());
    }

    @Test
    public void testGetResourceById_NotFound() {
        when(resourceService.getResourceById(1L)).thenThrow(new RuntimeException("Not found"));

        ResponseEntity<?> response = resourceController.getResourceById(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void testSearchResources() {
        ResourceResponse resp = new ResourceResponse();
        resp.setId(1L);

        when(resourceService.searchResources("query", "category")).thenReturn(Collections.singletonList(resp));

        ResponseEntity<List<ResourceResponse>> response = resourceController.searchResources("query", "category");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    public void testCreateResource_Success() {
        ResourceRequest req = new ResourceRequest();
        ResourceResponse resp = new ResourceResponse();
        resp.setId(2L);

        when(resourceService.createResource(any(ResourceRequest.class))).thenReturn(resp);

        ResponseEntity<?> response = resourceController.createResource(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(resp, response.getBody());
    }

    @Test
    public void testCreateResource_Failure() {
        ResourceRequest req = new ResourceRequest();

        when(resourceService.createResource(any(ResourceRequest.class))).thenThrow(new RuntimeException("Invalid URL format"));

        ResponseEntity<?> response = resourceController.createResource(req);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse body = (ApiResponse) response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals("Invalid URL format", body.getMessage());
    }

    @Test
    public void testUpdateResource_Success() {
        ResourceRequest req = new ResourceRequest();
        ResourceResponse resp = new ResourceResponse();
        resp.setId(1L);

        when(resourceService.updateResource(eq(1L), any(ResourceRequest.class))).thenReturn(resp);

        ResponseEntity<?> response = resourceController.updateResource(1L, req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(resp, response.getBody());
    }

    @Test
    public void testUpdateResource_Failure() {
        ResourceRequest req = new ResourceRequest();

        when(resourceService.updateResource(eq(1L), any(ResourceRequest.class))).thenThrow(new RuntimeException("Access denied"));

        ResponseEntity<?> response = resourceController.updateResource(1L, req);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse body = (ApiResponse) response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals("Access denied", body.getMessage());
    }

    @Test
    public void testDeleteResource_Success() {
        doNothing().when(resourceService).deleteResource(1L);

        ResponseEntity<?> response = resourceController.deleteResource(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse body = (ApiResponse) response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
        assertEquals("Resource deleted", body.getMessage());
    }

    @Test
    public void testDeleteResource_Failure() {
        doThrow(new RuntimeException("Access denied")).when(resourceService).deleteResource(1L);

        ResponseEntity<?> response = resourceController.deleteResource(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse body = (ApiResponse) response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals("Access denied", body.getMessage());
    }
}
