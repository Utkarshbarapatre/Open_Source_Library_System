package com.buzzword.osls.service;

import com.buzzword.osls.dto.CommentRequest;
import com.buzzword.osls.dto.CommentResponse;
import com.buzzword.osls.model.Comment;
import com.buzzword.osls.model.Resource;
import com.buzzword.osls.model.User;
import com.buzzword.osls.model.enums.Role;
import com.buzzword.osls.repository.CommentRepository;
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

import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    private User currentUser;
    private Resource testResource;

    @BeforeEach
    public void setUp() {
        SecurityContextHolder.clearContext();
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("testuser");
        currentUser.setEmail("test@test.com");
        currentUser.setRole(Role.USER);

        testResource = new Resource();
        testResource.setId(10L);
        testResource.setTitle("Resource Title");
    }

    private void setMockAuthentication(String username) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void testGetCommentsForResource() {
        Comment comment = new Comment();
        comment.setId(100L);
        comment.setContent("Hello World");
        comment.setUser(currentUser);
        comment.setResource(testResource);
        ReflectionTestUtils.setField(comment, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(comment, "updatedAt", LocalDateTime.now());

        when(commentRepository.findByResourceId(10L)).thenReturn(Collections.singletonList(comment));

        List<CommentResponse> responses = commentService.getCommentsForResource(10L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Hello World", responses.get(0).getContent());
        assertEquals("testuser", responses.get(0).getUsername());
    }

    @Test
    public void testGetCommentById_Success() {
        Comment comment = new Comment();
        comment.setId(100L);
        comment.setContent("Hello World");
        comment.setUser(currentUser);
        comment.setResource(testResource);

        when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));

        CommentResponse response = commentService.getCommentById(100L);

        assertNotNull(response);
        assertEquals("Hello World", response.getContent());
    }

    @Test
    public void testGetCommentById_NotFound() {
        when(commentRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            commentService.getCommentById(100L);
        }, "Comment not found");
    }

    @Test
    public void testAddComment() {
        setMockAuthentication("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(currentUser));
        when(resourceRepository.findById(10L)).thenReturn(Optional.of(testResource));

        CommentRequest req = new CommentRequest();
        req.setContent("   New Comment Content   ");

        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        CommentResponse response = commentService.addComment(10L, req);

        assertNotNull(response);
        assertEquals("New Comment Content", response.getContent());
        assertEquals("testuser", response.getUsername());
        assertEquals(10L, response.getResourceId());
    }

    @Test
    public void testUpdateComment_OwnerSuccess() {
        setMockAuthentication("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(currentUser));

        Comment comment = new Comment();
        comment.setId(100L);
        comment.setContent("Old Content");
        comment.setUser(currentUser);
        comment.setResource(testResource);

        when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> inv.getArgument(0));

        CommentRequest req = new CommentRequest();
        req.setContent("Updated Content");

        CommentResponse response = commentService.updateComment(100L, req);

        assertNotNull(response);
        assertEquals("Updated Content", response.getContent());
    }

    @Test
    public void testUpdateComment_AccessDenied() {
        setMockAuthentication("otheruser");
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otheruser");

        when(userRepository.findByUsername("otheruser")).thenReturn(Optional.of(otherUser));

        Comment comment = new Comment();
        comment.setId(100L);
        comment.setUser(currentUser); // owned by currentUser (id = 1)

        when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));

        CommentRequest req = new CommentRequest();

        assertThrows(RuntimeException.class, () -> {
            commentService.updateComment(100L, req);
        }, "Access denied: only the comment author can edit this comment");
    }

    @Test
    public void testDeleteComment_OwnerSuccess() {
        setMockAuthentication("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(currentUser));

        Comment comment = new Comment();
        comment.setId(100L);
        comment.setUser(currentUser);

        when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));
        doNothing().when(commentRepository).delete(comment);

        assertDoesNotThrow(() -> {
            commentService.deleteComment(100L);
        });

        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    public void testDeleteComment_AdminSuccess() {
        setMockAuthentication("adminuser");
        User adminUser = new User();
        adminUser.setId(3L);
        adminUser.setUsername("adminuser");
        adminUser.setRole(Role.ADMIN);

        when(userRepository.findByUsername("adminuser")).thenReturn(Optional.of(adminUser));

        Comment comment = new Comment();
        comment.setId(100L);
        comment.setUser(currentUser); // Owned by currentUser (role = USER)

        when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));
        doNothing().when(commentRepository).delete(comment);

        assertDoesNotThrow(() -> {
            commentService.deleteComment(100L);
        });

        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    public void testDeleteComment_AccessDenied() {
        setMockAuthentication("otheruser");
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otheruser");
        otherUser.setRole(Role.USER);

        when(userRepository.findByUsername("otheruser")).thenReturn(Optional.of(otherUser));

        Comment comment = new Comment();
        comment.setId(100L);
        comment.setUser(currentUser); // Owned by currentUser (id = 1)

        when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));

        assertThrows(RuntimeException.class, () -> {
            commentService.deleteComment(100L);
        }, "Access denied");

        verify(commentRepository, never()).delete(any());
    }
}
