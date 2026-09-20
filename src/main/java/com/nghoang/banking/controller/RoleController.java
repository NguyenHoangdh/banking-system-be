package com.nghoang.banking.controller;

import com.nghoang.banking.dto.ApiResponse;
import com.nghoang.banking.dto.request.RoleRequest;
import com.nghoang.banking.dto.response.RoleResponse;
import com.nghoang.banking.service.RoleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name ="Role Management APIs", description = "Các API quản lý vai trò người dùng")
public class RoleController {
    RoleService roleService;
    @PostMapping
    ApiResponse<RoleResponse> createRole(@RequestBody RoleRequest request) {
        return ApiResponse.<RoleResponse>builder()
                .code(1000)
                .message("Create role successfully!")
                .result(roleService.createRole(request))
                .build();
    }

    @GetMapping
    ApiResponse<List<RoleResponse>> getRoles() {
        return ApiResponse.<List<RoleResponse>>builder()
                .code(1000)
                .message("List roles:")
                .result(roleService.getRoles())
                .build();
    }

    @DeleteMapping("/roleName")
    ApiResponse<Void> deleteRole(@RequestParam String name) {
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Delete role successfully")
                .result(roleService.deleteRole(name))
                .build();
    }


}
