package com.spring.identity.configuration.seed;

import java.util.Arrays;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.spring.identity.repository.PermissionRepository;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class PermissionSeedConfig {

    @Bean
    @Order(1)
    @ConditionalOnProperty(
            prefix = "spring",
            value = "datasource.driverClassName",
            havingValue = "com.mysql.cj.jdbc.Driver")
    ApplicationRunner permissionSeeder(PermissionRepository permissionRepository) {
        return args -> {
            if (permissionRepository.count() > 0) {
                return;
            }

            var permissions = Arrays.stream(com.spring.identity.enums.Permission.values())
                    .map(permission -> com.spring.identity.entity.Permission.builder()
                            .name(permission.name())
                            .description(permission.name() + " permission")
                            .build())
                    .toList();

            permissionRepository.saveAll(permissions);
            log.info("Seeded {} default permissions", permissions.size());
        };
    }
}
