package com.buzzword.osls.service;

import com.buzzword.osls.dto.UserResponse;
import com.buzzword.osls.model.User;
import com.buzzword.osls.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
            .map(u -> new UserResponse(u.getId(), u.getUsername(), u.getEmail(), u.getRole().name(), u.getCreatedAt()))
            .collect(Collectors.toList());
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public UserResponse getUserResponseById(Long id) {
        User u=getUserById(id);
        return new UserResponse(u.getId(), u.getUsername(), u.getEmail(), u.getRole().name(), u.getCreatedAt());
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }
}
