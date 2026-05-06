package com.spring.identity.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.spring.identity.entity.RoleResourcePermission;

public interface RoleResourcePermissionRepository extends JpaRepository<RoleResourcePermission, Long> {
    boolean existsByRoleNameAndResourceCodeAndPermissionName(
            String roleName, String resourceCode, String permissionName);
}
