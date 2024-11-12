package com.vanh.demo_spring.mapper;

import org.mapstruct.Mapper;

import com.vanh.demo_spring.dto.request.PermissionRequest;
import com.vanh.demo_spring.dto.response.PermissionResponse;
import com.vanh.demo_spring.entity.Permission;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}
