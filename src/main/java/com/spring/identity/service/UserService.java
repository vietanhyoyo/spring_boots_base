package com.spring.identity.service;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.spring.identity.constant.PredefinedRole;
import com.spring.identity.dto.request.GoogleRegisterRequest;
import com.spring.identity.dto.request.UserCreationRequest;
import com.spring.identity.dto.request.UserUpdateRequest;
import com.spring.identity.dto.response.GoogleTokenInfoResponse;
import com.spring.identity.dto.response.UserResponse;
import com.spring.identity.entity.Role;
import com.spring.identity.entity.User;
import com.spring.identity.exception.AppException;
import com.spring.identity.exception.ErrorCode;
import com.spring.identity.mapper.UserMapper;
import com.spring.identity.repository.RoleRepository;
import com.spring.identity.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {

    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @NonFinal
    @Value("${google.client-id:}")
    String googleClientId;

    public UserResponse createUser(UserCreationRequest request) {
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        HashSet<Role> roles = new HashSet<>();
        roleRepository.findByName(PredefinedRole.USER_ROLE).ifPresent(roles::add);

        user.setRoles(roles);

        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        return userMapper.toUserResponse(user);
    }

    public UserResponse createGoogleUser(GoogleRegisterRequest request) {
        GoogleTokenInfoResponse tokenInfo = verifyGoogleIdToken(request.getIdToken());

        if (userRepository.findByEmail(tokenInfo.getEmail()).isPresent()) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        HashSet<Role> roles = new HashSet<>();
        roleRepository.findByName(PredefinedRole.USER_ROLE).ifPresent(roles::add);

        User user = User.builder()
                .username(buildGoogleUsername(tokenInfo))
                .email(tokenInfo.getEmail())
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .firstName(tokenInfo.getGivenName())
                .lastName(tokenInfo.getFamilyName())
                .roles(roles)
                .build();

        try {
            return userMapper.toUserResponse(userRepository.save(user));
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
    }

    private GoogleTokenInfoResponse verifyGoogleIdToken(String idToken) {
        if (!StringUtils.hasText(idToken) || !StringUtils.hasText(googleClientId)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        try {
            GoogleTokenInfoResponse tokenInfo = RestClient.create("https://oauth2.googleapis.com")
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/tokeninfo")
                            .queryParam("id_token", idToken)
                            .build())
                    .retrieve()
                    .body(GoogleTokenInfoResponse.class);

            if (tokenInfo == null
                    || !googleClientId.equals(tokenInfo.getAud())
                    || !"true".equalsIgnoreCase(tokenInfo.getEmailVerified())
                    || !StringUtils.hasText(tokenInfo.getEmail())) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }

            return tokenInfo;
        } catch (RestClientException exception) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    private String buildGoogleUsername(GoogleTokenInfoResponse tokenInfo) {
        if (StringUtils.hasText(tokenInfo.getName())) {
            return tokenInfo.getName();
        }

        return tokenInfo.getEmail().substring(0, tokenInfo.getEmail().indexOf("@"));
    }

    public List<UserResponse> getUsers() {
        log.info("In method get users");
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }

    @PostAuthorize("returnObject.email == authentication.name")
    public UserResponse getUser(Long id) {
        return userMapper.toUserResponse(
                userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }

    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        userMapper.updateUser(user, request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        var roles = roleRepository.findAllByNameIn(request.getRoles());
        user.setRoles(new HashSet<>(roles));

        return userMapper.toUserResponse(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String email = context.getAuthentication().getName();

        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toUserResponse(user);
    }
}
