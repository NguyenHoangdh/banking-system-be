package com.nghoang.banking.service;

import com.nghoang.banking.dto.request.PermissionRequest;
import com.nghoang.banking.dto.response.PermissionResponse;

import java.util.List;

public interface PermissionService {
    PermissionResponse createPermission(PermissionRequest request);
    List<PermissionResponse> getPermission();
    Void deletePermission(String name);

    }
