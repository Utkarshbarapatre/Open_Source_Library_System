package com.buzzword.osls.service;

import com.buzzword.osls.dto.UserResponse;
import com.buzzword.osls.model.User;
import com.buzzword.osls.model.enums.Role;
import com.buzzword.osls.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    public void testGetAllUsers() {
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setEmail("user1@test.com");
        user1.setRole(Role.USER);
        ReflectionTestUtils.setField(user1, "createdAt", LocalDateTime.now());

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@test.com");
        user2.setRole(Role.ADMIN);
        ReflectionTestUtils.setField(user2, "createdAt", LocalDateTime.now());

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        List<UserResponse> responses = userService.getAllUsers();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("user1", responses.get(0).getUsername());
        assertEquals("ADMIN", responses.get(1).getRole());
    }

    @Test
    public void testGetUserById_Success() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
    }

    @Test
    public void testGetUserById_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            userService.getUserById(1L);
        }, "User not found");
    }

    @Test
    public void testGetUserResponseById() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setRole(Role.USER);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse result = userService.getUserResponseById(1L);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("USER", result.getRole());
    }

    @Test
    public void testDeleteUser_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        assertDoesNotThrow(() -> {
            userService.deleteUser(1L);
        });

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteUser_NotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            userService.deleteUser(1L);
        }, "User not found");

        verify(userRepository, never()).deleteById(anyLong());
    }
}
