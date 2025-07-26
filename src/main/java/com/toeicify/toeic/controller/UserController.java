package com.toeicify.toeic.controller;

import com.toeicify.toeic.dto.request.user.UpdatePasswordRequest;
import com.toeicify.toeic.dto.request.user.UpdateUserRequest;
import com.toeicify.toeic.dto.response.ApiResponse;
import com.toeicify.toeic.dto.response.PaginationResponse;
import com.toeicify.toeic.dto.response.user.AdminUpdateUserResponse;
import com.toeicify.toeic.dto.response.user.UserUpdateResponse;
import com.toeicify.toeic.entity.User;
import com.toeicify.toeic.service.UserService;
import com.toeicify.toeic.util.annotation.ApiMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
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

    // @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("")
    @ApiMessage("Get paginated list of users")
    public ApiResponse<PaginationResponse> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "") String searchTerm) {
        page = page - 1;
        return ApiResponse.success(userService.getUsers(searchTerm,page, size));
    }

    @PatchMapping("/{userId}/toggle-status")
    @ApiMessage("Toggle user status")
    public ApiResponse<AdminUpdateUserResponse> toggleUserStatus(@PathVariable Long userId) {
        User updatedUser = userService.toggleUserStatus(userId);
        AdminUpdateUserResponse response = AdminUpdateUserResponse.from(updatedUser);
        return ApiResponse.success(response);
    }

}
