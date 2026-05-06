package com.spring.identity.service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.spring.identity.dto.request.RoleRequest;
import com.spring.identity.dto.response.RoleResponse;
import com.spring.identity.entity.Role;
import com.spring.identity.entity.RoleResourcePermission;
import com.spring.identity.mapper.RoleMapper;
import com.spring.identity.repository.PermissionRepository;
import com.spring.identity.repository.ResourceRepository;
import com.spring.identity.repository.RoleRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RoleService {
    RoleRepository roleRepository;
    PermissionRepository permissionRepository;
    ResourceRepository resourceRepository;
    RoleMapper roleMapper;

    public RoleResponse create(RoleRequest request) {
        Role role = roleMapper.toRole(request);

        if (Objects.nonNull(request.getResourcePermissions())) {
            Role roleRef = role;
            var resourcePermissions = new HashSet<RoleResourcePermission>();

            request.getResourcePermissions().forEach(resourcePermission -> {
                var resource = resourceRepository
                        .findByCode(resourcePermission.getResource())
                        .orElse(null);
                var permissions = permissionRepository.findAllByNameIn(resourcePermission.getPermissions());

                permissions.forEach(permission -> resourcePermissions.add(RoleResourcePermission.builder()
                        .role(roleRef)
                        .resource(resource)
                        .permission(permission)
                        .build()));
            });

            role.setResourcePermissions(resourcePermissions);
        }

        role = roleRepository.save(role);

        return roleMapper.toRoleResponse(role);
    }

    public List<RoleResponse> getAll() {
        var roles = roleRepository.findAll();
        return roles.stream().map(roleMapper::toRoleResponse).toList();
    }

    public void delete(String role) {
        roleRepository.findByName(role).ifPresent(roleRepository::delete);
    }
}
