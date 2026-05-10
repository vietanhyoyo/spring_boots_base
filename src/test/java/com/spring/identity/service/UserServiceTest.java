package com.spring.identity.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.spring.identity.dto.request.UserCreationRequest;
import com.spring.identity.dto.response.UserResponse;
import com.spring.identity.entity.User;
import com.spring.identity.exception.AppException;
import com.spring.identity.mapper.UserMapper;
import com.spring.identity.repository.RoleRepository;
import com.spring.identity.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private GoogleTokenService googleTokenService;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserService userService;

    private UserCreationRequest request;
    private UserResponse userResponse;
    private User user;

    @BeforeEach
    void intiData() {
        LocalDate dob = LocalDate.of(1990, 1, 1);
        request = UserCreationRequest.builder()
                .username("john")
                .email("john@example.com")
                .firstName("John")
                .lastName("Doe")
                .password("12345678")
                .dob(dob)
                .build();

        user = User.builder()
                .id(1L)
                .username("john")
                .email("john@example.com")
                .firstName("John")
                .lastName("Doe")
                .dob(dob)
                .build();

        userResponse = UserResponse.builder()
                .id(1L)
                .username("john")
                .email("john@example.com")
                .firstName("John")
                .lastName("Doe")
                .dob(dob)
                .build();
    }

    @Test
    void createUser_validRequest_success() {
        // GIVEN
        when(userMapper.toUser(request)).thenReturn(user);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        // WHEN
        var response = userService.createUser(request);

        // THEN
        Assertions.assertThat(response.getId()).isEqualTo(1L);
        Assertions.assertThat(response.getUsername()).isEqualTo("john");
    }

    @Test
    void createUser_userExisted_fail() {
        // GIVEN
        when(userMapper.toUser(request)).thenReturn(user);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class)))
                .thenThrow(new org.springframework.dao.DataIntegrityViolationException(""));

        // WHEN
        var exception = assertThrows(AppException.class, () -> userService.createUser(request));

        // THEN
        Assertions.assertThat(exception.getErrorCode().getCode()).isEqualTo(1002);
    }

    @Test
    void getMyInfo_valid_success() {
        // GIVEN
        mockAuthenticationName("john@example.com");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        // WHEN
        var response = userService.getMyInfo();

        // THEN
        Assertions.assertThat(response.getUsername()).isEqualTo("john");
        Assertions.assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void getMyInfo_valid_error() {
        // GIVEN
        mockAuthenticationName("john@example.com");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());

        // WHEN
        var exception = assertThrows(AppException.class, () -> userService.getMyInfo());

        // THEN
        Assertions.assertThat(exception.getErrorCode().getCode()).isEqualTo(1005);
    }

    private void mockAuthenticationName(String name) {
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        SecurityContext securityContext = org.mockito.Mockito.mock(SecurityContext.class);

        when(authentication.getName()).thenReturn(name);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
}
