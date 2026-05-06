package com.spring.identity.configuration.seed;

import java.util.Arrays;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.spring.identity.repository.ResourceRepository;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class ResourceSeedConfig {

    @Bean
    @Order(2)
    @ConditionalOnProperty(
            prefix = "spring",
            value = "datasource.driverClassName",
            havingValue = "com.mysql.cj.jdbc.Driver")
    ApplicationRunner resourceSeeder(ResourceRepository resourceRepository) {
        return args -> {
            if (resourceRepository.count() > 0) {
                return;
            }

            var resources = Arrays.stream(com.spring.identity.enums.Resource.values())
                    .map(resource -> com.spring.identity.entity.Resource.builder()
                            .code(resource.name())
                            .name(resource.name())
                            .description(resource.name() + " resource")
                            .build())
                    .toList();

            resourceRepository.saveAll(resources);
            log.info("Seeded {} default resources", resources.size());
        };
    }
}
