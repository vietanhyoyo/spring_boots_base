package com.vanh.demo_spring.mapper;

import com.vanh.demo_spring.dto.request.RoleRequest;
import com.vanh.demo_spring.dto.response.RoleResponse;
import com.vanh.demo_spring.entity.Permission;
import com.vanh.demo_spring.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest role);

    RoleResponse toRoleResponse(Role role);
}
