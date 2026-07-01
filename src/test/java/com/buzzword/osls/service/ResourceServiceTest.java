package com.buzzword.osls.service;

import com.buzzword.osls.dto.ResourceRequest;
import com.buzzword.osls.dto.ResourceResponse;
import com.buzzword.osls.model.Resource;
import com.buzzword.osls.model.User;
import com.buzzword.osls.model.enums.ResourceCategory;
import com.buzzword.osls.model.enums.Role;
import com.buzzword.osls.repository.ResourceRepository;
import com.buzzword.osls.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ResourceServiceTest {

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ResourceService resourceService;

    private User currentUser;

    @BeforeEach
    public void setUp() {
        SecurityContextHolder.clearContext();
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("testuser");
        currentUser.setEmail("test@test.com");
        currentUser.setRole(Role.USER);
    }

    private void setMockAuthentication(String username) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void testGetAllResources() {
        Resource r = new Resource();
        r.setId(10L);
        r.setTitle("Title");
        r.setDescription("Desc");
        r.setUrl("http://example.com");
        r.setCategory(ResourceCategory.BOOK);
        r.setAddedBy(currentUser);

        when(resourceRepository.findAll()).thenReturn(Collections.singletonList(r));

        List<ResourceResponse> responses = resourceService.getAllResources();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Title", responses.get(0).getTitle());
        assertEquals("BOOK", responses.get(0).getCategory());
    }

    @Test
    public void testGetResourceById_Success() {
        Resource r = new Resource();
        r.setId(10L);
        r.setTitle("Title");
        r.setUrl("http://example.com");
        r.setCategory(ResourceCategory.BOOK);
        r.setAddedBy(currentUser);

        when(resourceRepository.findById(10L)).thenReturn(Optional.of(r));

        ResourceResponse resp = resourceService.getResourceById(10L);

        assertNotNull(resp);
        assertEquals("Title", resp.getTitle());
    }

    @Test
    public void testGetResourceById_NotFound() {
        when(resourceRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            resourceService.getResourceById(10L);
        }, "Resource not found");
    }

    @Test
    public void testCreateResource_Success() {
        setMockAuthentication("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(currentUser));

        ResourceRequest req = new ResourceRequest();
        req.setTitle("Book Title");
        req.setDescription("Book Description");
        req.setUrl("https://google.com");
        req.setCategory("BOOK");

        when(resourceRepository.save(any(Resource.class))).thenAnswer(invocation -> {
            Resource saved = invocation.getArgument(0);
            saved.setId(123L);
            return saved;
        });

        ResourceResponse resp = resourceService.createResource(req);

        assertNotNull(resp);
        assertEquals(123L, resp.getId());
        assertEquals("Book Title", resp.getTitle());
        assertEquals("BOOK", resp.getCategory());
        assertEquals("testuser", resp.getAddedByUsername());
    }

    @Test
    public void testCreateResource_InvalidUrl() {
        ResourceRequest req = new ResourceRequest();
        req.setUrl("invalid-url-format");

        assertThrows(RuntimeException.class, () -> {
            resourceService.createResource(req);
        }, "Invalid URL format");
    }

    @Test
    public void testUpdateResource_OwnerSuccess() {
        setMockAuthentication("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(currentUser));

        Resource existing = new Resource();
        existing.setId(10L);
        existing.setTitle("Old Title");
        existing.setUrl("https://old.com");
        existing.setCategory(ResourceCategory.BOOK);
        existing.setAddedBy(currentUser);

        when(resourceRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(resourceRepository.save(any(Resource.class))).thenAnswer(inv -> inv.getArgument(0));

        ResourceRequest req = new ResourceRequest();
        req.setTitle("New Title");
        req.setDescription("New Desc");
        req.setUrl("https://new.com");
        req.setCategory("TOOL");

        ResourceResponse resp = resourceService.updateResource(10L, req);

        assertNotNull(resp);
        assertEquals("New Title", resp.getTitle());
        assertEquals("TOOL", resp.getCategory());
    }

    @Test
    public void testUpdateResource_AccessDenied() {
        setMockAuthentication("otheruser");
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otheruser");
        otherUser.setRole(Role.USER);

        when(userRepository.findByUsername("otheruser")).thenReturn(Optional.of(otherUser));

        Resource existing = new Resource();
        existing.setId(10L);
        existing.setAddedBy(currentUser);

        when(resourceRepository.findById(10L)).thenReturn(Optional.of(existing));

        ResourceRequest req = new ResourceRequest();

        assertThrows(RuntimeException.class, () -> {
            resourceService.updateResource(10L, req);
        }, "Access denied");
    }

    @Test
    public void testDeleteResource_AdminSuccess() {
        setMockAuthentication("adminuser");
        User adminUser = new User();
        adminUser.setId(3L);
        adminUser.setUsername("adminuser");
        adminUser.setRole(Role.ADMIN);

        when(userRepository.findByUsername("adminuser")).thenReturn(Optional.of(adminUser));

        Resource existing = new Resource();
        existing.setId(10L);
        existing.setAddedBy(currentUser);

        when(resourceRepository.findById(10L)).thenReturn(Optional.of(existing));
        doNothing().when(resourceRepository).delete(existing);

        assertDoesNotThrow(() -> {
            resourceService.deleteResource(10L);
        });

        verify(resourceRepository, times(1)).delete(existing);
    }

    @Test
    public void testSearchResources() {
        Resource r = new Resource();
        r.setId(1L);
        r.setTitle("Search result");
        r.setUrl("http://example.com");
        r.setCategory(ResourceCategory.BOOK);
        r.setAddedBy(currentUser);

        when(resourceRepository.searchResourcesByQueryAndCategory("query", ResourceCategory.BOOK))
                .thenReturn(Collections.singletonList(r));

        List<ResourceResponse> responses = resourceService.searchResources("query", "BOOK");

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Search result", responses.get(0).getTitle());
    }
}
