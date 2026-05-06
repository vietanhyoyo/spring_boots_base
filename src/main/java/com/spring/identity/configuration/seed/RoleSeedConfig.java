package com.spring.identity.configuration.seed;

import java.util.Arrays;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.spring.identity.entity.Role;
import com.spring.identity.repository.RoleRepository;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class RoleSeedConfig {

    @Bean
    @Order(3)
    @ConditionalOnProperty(
            prefix = "spring",
            value = "datasource.driverClassName",
            havingValue = "com.mysql.cj.jdbc.Driver")
    ApplicationRunner roleSeeder(RoleRepository roleRepository) {
        return args -> {
            if (roleRepository.count() > 0) {
                return;
            }

            var roles = Arrays.stream(com.spring.identity.enums.Role.values())
                    .map(role -> Role.builder()
                            .name(role.name())
                            .description(role.name() + " role")
                            .build())
                    .toList();

            roleRepository.saveAll(roles);
            log.info("Seeded {} default roles", roles.size());
        };
    }
}
