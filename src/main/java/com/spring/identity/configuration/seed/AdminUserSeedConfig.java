package com.spring.identity.configuration.seed;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.spring.identity.entity.User;
import com.spring.identity.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminUserSeedConfig {

    PasswordEncoder passwordEncoder;

    @Bean
    @Order(5)
    @ConditionalOnProperty(
            prefix = "spring",
            value = "datasource.driverClassName",
            havingValue = "com.mysql.cj.jdbc.Driver")
    ApplicationRunner adminUserSeeder(UserRepository userRepository) {
        return args -> {
            if (userRepository.findByUsername("admin").isPresent()) {
                return;
            }

            User user = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("12345678"))
                    .build();

            userRepository.save(user);
            log.warn("admin user has been created with default password: admin, please change it ");
        };
    }
}
