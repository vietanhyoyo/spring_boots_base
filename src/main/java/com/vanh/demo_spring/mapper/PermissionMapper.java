package com.vanh.demo_spring.mapper;

import com.vanh.demo_spring.dto.request.PermissionRequest;
import com.vanh.demo_spring.dto.response.PermissionResponse;
import com.vanh.demo_spring.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);
    PermissionResponse toPermissionResponse(Permission permission);
}
