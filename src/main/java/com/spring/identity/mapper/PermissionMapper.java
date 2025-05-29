package com.spring.identity.mapper;

import org.mapstruct.Mapper;

import com.spring.identity.dto.request.PermissionRequest;
import com.spring.identity.dto.response.PermissionResponse;
import com.spring.identity.entity.Permission;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}
