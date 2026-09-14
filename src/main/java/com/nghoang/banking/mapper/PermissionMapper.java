package com.nghoang.banking.mapper;

import com.nghoang.banking.dto.request.PermissionRequest;
import com.nghoang.banking.dto.response.PermissionResponse;
import com.nghoang.banking.entity.Permission;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);
    PermissionResponse toPermissionResponse(Permission permission);
}
