package com.nghoang.banking.service;

import com.nghoang.banking.dto.request.RoleRequest;
import com.nghoang.banking.dto.response.RoleResponse;
import java.util.List;

public interface RoleService {
    RoleResponse createRole(RoleRequest request);
    List<RoleResponse> getRoles();
    Void deleteRole(String name);
}