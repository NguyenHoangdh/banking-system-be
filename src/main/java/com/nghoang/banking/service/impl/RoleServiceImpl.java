package com.nghoang.banking.service.impl;

import com.nghoang.banking.dto.request.RoleRequest;
import com.nghoang.banking.dto.response.RoleResponse;
import com.nghoang.banking.entity.Role;
import com.nghoang.banking.exception.AppException;
import com.nghoang.banking.exception.ErrorCode;
import com.nghoang.banking.mapper.RoleMapper;
import com.nghoang.banking.repository.PermissionRepository;
import com.nghoang.banking.repository.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleServiceImpl implements RoleService{
    RoleRepository roleRepository;
    RoleMapper roleMapper;
    PermissionRepository permissionRepository;
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public RoleResponse createRole(RoleRequest request) {
        if (roleRepository.existsById(request.getName())) {
            throw new AppException(ErrorCode.ROLE_EXISTED);
        }
        Role role = roleMapper.toRole(request);
        var permissions = permissionRepository.findAllById(request.getPermissions());
        role.setPermissions(new HashSet<>(permissions));
        return roleMapper.toRoleResponse(roleRepository.save(role));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<RoleResponse> getRoles() {
        var roles = roleRepository.findAll();
        return  roles.stream().map(roleMapper::toRoleResponse).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Void deleteRole(String name) {
        if (!roleRepository.existsById(name)) {
            throw new AppException(ErrorCode.ROLE_NOT_EXISTED);
        }
        roleRepository.deleteById(name);
        return null;
    }

}
