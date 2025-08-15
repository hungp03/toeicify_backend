package com.toeicify.toeic.controller;

import com.toeicify.toeic.dto.request.user.ToggleStatusRequest;
import com.toeicify.toeic.dto.request.user.UpdatePasswordRequest;
import com.toeicify.toeic.dto.request.user.UpdateUserRequest;
import com.toeicify.toeic.dto.response.PaginationResponse;
import com.toeicify.toeic.dto.response.user.AdminUserResponse;
import com.toeicify.toeic.dto.response.user.UserUpdateResponse;
import com.toeicify.toeic.entity.User;
import com.toeicify.toeic.service.UserService;
import com.toeicify.toeic.util.annotation.ApiMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by hungpham on 7/9/2025
 */
@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @PutMapping("me")
    @ApiMessage("Update current user")
    public ResponseEntity<UserUpdateResponse> updateCurrentUser(@Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateCurrentUser(request));
    }

    @PatchMapping("password")
    @ApiMessage("Change password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody UpdatePasswordRequest request){
        userService.changePassword(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @ApiMessage("Get list of users")
    public ResponseEntity<PaginationResponse> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "", required = false) String searchTerm) {
        return ResponseEntity.ok(userService.getUsers(searchTerm, page, size));
    }

    @PatchMapping("/{userId}/toggle-status")
    @ApiMessage("Toggle user status")
    public ResponseEntity<Void> toggleUserStatus(
            @PathVariable Long userId,
            @RequestBody(required = false) ToggleStatusRequest request) {
        userService.toggleUserStatus(userId, request != null ? request.lockReason() : null);
        return ResponseEntity.ok().build();
    }
}
