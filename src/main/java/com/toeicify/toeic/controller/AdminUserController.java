package com.toeicify.toeic.controller;

import com.toeicify.toeic.dto.request.user.UpdatePasswordRequest;
import com.toeicify.toeic.dto.request.user.UpdateUserRequest;
import com.toeicify.toeic.dto.response.user.UserUpdateResponse;
import com.toeicify.toeic.service.UserService;
import com.toeicify.toeic.util.annotation.ApiMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import com.toeicify.toeic.dto.response.PaginationResponse;
import com.toeicify.toeic.dto.response.ApiResponse;
import com.toeicify.toeic.entity.User;
import com.toeicify.toeic.dto.response.user.AdminUpdateUserResponse;

@RestController
@RequestMapping("api/admin")
@RequiredArgsConstructor
public class AdminUserController {
    private final UserService userService;

    // @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    @ApiMessage("Get paginated list of users")
    public ApiResponse<PaginationResponse> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "") String searchTerm) {
        page = page - 1;
        Pageable pageable = PageRequest.of(page, size);
        // Page<User> userPage = userService.getUsers(searchTerm, pageable);
        // PaginationResponse paginationResponse = PaginationResponse.from(userPage, pageable);
        Page<User> userPage = userService.getUsers(searchTerm, pageable);
        // Page<AdminUpdateUserResponse> responsePage = userPage.map(AdminUpdateUserResponse::from);
        Page<AdminUpdateUserResponse> responsePage = userPage.map(user -> AdminUpdateUserResponse.from(user));
        PaginationResponse paginationResponse = PaginationResponse.from(responsePage, pageable);
        return ApiResponse.success(paginationResponse);
    }

    @PutMapping("/users/{userId}/toggle-status")
    @ApiMessage("Toggle user status")
    public ApiResponse<AdminUpdateUserResponse> toggleUserStatus(@PathVariable Long userId) {
        User user = userService.findById(userId);
        user.setIsActive(!user.getIsActive());
        userService.updateUser(user);
        AdminUpdateUserResponse response = AdminUpdateUserResponse.from(user);

        return ApiResponse.success(response);
    }
}

// package com.toeicify.toeic.controller;

// import com.toeicify.toeic.dto.response.ApiResponse;
// import com.toeicify.toeic.dto.response.PaginationResponse;
// import com.toeicify.toeic.dto.response.user.AdminUpdateUserResponse;
// import com.toeicify.toeic.entity.User;
// import com.toeicify.toeic.service.UserService;
// import com.toeicify.toeic.util.annotation.ApiMessage;
// import lombok.RequiredArgsConstructor;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.data.domain.Pageable;
// import org.springframework.web.bind.annotation.*;

// @RestController
// @RequestMapping("/api/admin")
// @RequiredArgsConstructor
// public class AdminUserController {
//     private final UserService userService;

//     @GetMapping("/users")
//     @ApiMessage("Get paginated list of users")
//     public ApiResponse<PaginationResponse> getUsers(
//             @RequestParam(defaultValue = "0") int page,
//             @RequestParam(defaultValue = "20") int size,
//             @RequestParam(defaultValue = "") String searchTerm) {
//         Pageable pageable = PageRequest.of(page, size);
//         Page<User> userPage = userService.getUsers(searchTerm, pageable);
//         PaginationResponse paginationResponse = PaginationResponse.from(userPage, pageable);
//         return ApiResponse.success(paginationResponse);
//     }

//     @PutMapping("/{userId}/toggle-status")
//     @ApiMessage("Toggle user status")
//     public ApiResponse<User> toggleUserStatus(@PathVariable Long userId) {
//         User user = userService.findById(userId);
//         user.setIsActive(!user.getIsActive());
//         userService.updateUser(user);
//         AdminUpdateUserResponse response = AdminUpdateUserResponse.from(user);
//         return ApiResponse.success(response);
//     }
// }