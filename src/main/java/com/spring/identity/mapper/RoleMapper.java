package com.spring.identity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.spring.identity.dto.request.RoleRequest;
import com.spring.identity.dto.response.RoleResponse;
import com.spring.identity.entity.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest role);

    RoleResponse toRoleResponse(Role role);
}
