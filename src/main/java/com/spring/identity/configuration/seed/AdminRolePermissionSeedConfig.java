package com.spring.identity.configuration.seed;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.spring.identity.constant.PredefinedRole;
import com.spring.identity.entity.Role;
import com.spring.identity.entity.RoleResourcePermission;
import com.spring.identity.repository.PermissionRepository;
import com.spring.identity.repository.ResourceRepository;
import com.spring.identity.repository.RoleRepository;
import com.spring.identity.repository.RoleResourcePermissionRepository;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class AdminRolePermissionSeedConfig {

    @Bean
    @Order(4)
    @ConditionalOnProperty(
            prefix = "spring",
            value = "datasource.driverClassName",
            havingValue = "com.mysql.cj.jdbc.Driver")
    ApplicationRunner adminRolePermissionSeeder(
            RoleRepository roleRepository,
            ResourceRepository resourceRepository,
            PermissionRepository permissionRepository,
            RoleResourcePermissionRepository roleResourcePermissionRepository) {
        return args -> {
            Role adminRole = roleRepository
                    .findByName(PredefinedRole.ADMIN_ROLE)
                    .orElseGet(() -> roleRepository.save(Role.builder()
                            .name(PredefinedRole.ADMIN_ROLE)
                            .description(PredefinedRole.ADMIN_ROLE + " role")
                            .build()));

            var resources = resourceRepository.findAll();
            var permissions = permissionRepository.findAll();
            int adminPermissionCount = 0;

            for (var resource : resources) {
                for (var permission : permissions) {
                    boolean existed = roleResourcePermissionRepository.existsByRoleNameAndResourceCodeAndPermissionName(
                            adminRole.getName(), resource.getCode(), permission.getName());

                    if (!existed) {
                        roleResourcePermissionRepository.save(RoleResourcePermission.builder()
                                .role(adminRole)
                                .resource(resource)
                                .permission(permission)
                                .build());
                        adminPermissionCount++;
                    }
                }
            }

            if (adminPermissionCount > 0) {
                log.info("Seeded {} permissions for ADMIN role", adminPermissionCount);
            }
        };
    }
}
