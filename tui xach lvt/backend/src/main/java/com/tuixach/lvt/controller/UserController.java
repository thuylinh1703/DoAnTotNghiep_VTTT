package com.tuixach.lvt.controller;

import com.tuixach.lvt.dto.*;
import com.tuixach.lvt.entity.User;
import com.tuixach.lvt.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> getProfile(@AuthenticationPrincipal User user) {
        UserDTO userDTO = userService.getUserProfile(user);
        return ResponseEntity.ok(ApiResponse.success(userDTO));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> updateProfile(
            @AuthenticationPrincipal User user,
            @RequestBody UpdateUserRequest request) {
        UserDTO userDTO = userService.updateProfile(user, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin thành công", userDTO));
    }
}
