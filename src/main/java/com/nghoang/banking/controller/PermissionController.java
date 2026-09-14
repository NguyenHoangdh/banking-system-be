package com.nghoang.banking.controller;

import com.nghoang.banking.dto.ApiResponse;
import com.nghoang.banking.dto.request.PermissionRequest;
import com.nghoang.banking.dto.response.PermissionResponse;
import com.nghoang.banking.service.impl.PermissionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Permission Management APIs", description = "Các API quản lý quyền hạn người dùng")
public class PermissionController {
    PermissionService permissionService;
    @PostMapping
    ApiResponse<PermissionResponse> createPermission(@RequestBody PermissionRequest request) {
        return ApiResponse.<PermissionResponse>builder()
                .code(1000)
                .message("Create permission successfully")
                .result(permissionService.createPermission(request))
                .build();
    }

    @GetMapping
    ApiResponse<List<PermissionResponse>> getPermissions() {
        return ApiResponse.<List<PermissionResponse>>builder()
                .code(1000)
                .message("List permissions:")
                .result(permissionService.getPermission())
                .build();
    }
    @DeleteMapping
    ApiResponse<Void> deletePermission(@RequestParam String name) {
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Delete permission successfully")
                .result(permissionService.deletePermission(name))
                .build();
    }




}
