package com.nghoang.banking.service.impl;

import com.nghoang.banking.dto.request.PermissionRequest;
import com.nghoang.banking.dto.response.PermissionResponse;

import java.util.List;

public interface PermissionService {
    PermissionResponse createPermission(PermissionRequest request);
    public List<PermissionResponse> getPermission();
    public Void deletePermission(String name);

    }
