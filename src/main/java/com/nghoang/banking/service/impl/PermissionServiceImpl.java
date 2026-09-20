package com.nghoang.banking.service.impl;

import com.nghoang.banking.dto.request.PermissionRequest;
import com.nghoang.banking.dto.response.PermissionResponse;
import com.nghoang.banking.entity.Permission;
import com.nghoang.banking.exception.AppException;
import com.nghoang.banking.exception.ErrorCode;
import com.nghoang.banking.mapper.PermissionMapper;
import com.nghoang.banking.repository.PermissionRepository;
import com.nghoang.banking.service.PermissionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionServiceImpl implements PermissionService {
    PermissionRepository permissionRepository;
    PermissionMapper permissionMapper;
    @PreAuthorize("hasRole('ADMIN')")
    public PermissionResponse createPermission(PermissionRequest request) {
        if (permissionRepository.existsById(request.getName())) {
            throw new AppException(ErrorCode.PERMISSION_EXISTED);
        }
        Permission permission = permissionMapper.toPermission(request);
        return permissionMapper.toPermissionResponse(permissionRepository.save(permission));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<PermissionResponse> getPermission() {
        var permissions = permissionRepository.findAll();
        return  permissions.stream().map(permissionMapper::toPermissionResponse).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Void deletePermission(String name) {
        if (!permissionRepository.existsById(name)) {
            throw new AppException(ErrorCode.PERMISSION_NOT_EXISTED);
        }
        permissionRepository.deleteById(name);
        return null;
    }
}
