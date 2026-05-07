package com.tuixach.lvt.service;

import com.tuixach.lvt.dto.UpdateUserRequest;
import com.tuixach.lvt.dto.UserDTO;
import com.tuixach.lvt.entity.User;
import com.tuixach.lvt.exception.ResourceNotFoundException;
import com.tuixach.lvt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserDTO getUserProfile(User user) {
        return mapToDTO(user);
    }

    public UserDTO updateProfile(User user, UpdateUserRequest request) {
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        userRepository.save(user);
        return mapToDTO(user);
    }

    // Admin methods
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::mapToDTO);
    }

    public void toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng");
        }
        userRepository.deleteById(userId);
    }

    public
    List<UserDTO> getSupportCustomers() {
        return userRepository.findByRoleOrderByCreatedAtDesc(User.Role.USER)
                .stream()
                .limit(10)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private UserDTO mapToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .role(user.getRole().name())
                .active(user.isActive())
                .build();
    }
}
