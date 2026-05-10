package com.spring.identity.controller;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.nimbusds.jose.JOSEException;
import com.spring.identity.dto.request.*;
import com.spring.identity.dto.response.AuthenticationResponse;
import com.spring.identity.dto.response.IntrospectResponse;
import com.spring.identity.exception.AppException;
import com.spring.identity.exception.ErrorCode;
import com.spring.identity.service.AuthenticationService;
import com.spring.identity.service.GoogleTokenService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    static int GOOGLE_OAUTH_STATE_MAX_AGE_SECONDS = 300;

    AuthenticationService authenticationService;
    GoogleTokenService googleTokenService;

    @NonFinal
    @Value("${frontend.url:http://localhost:3000}")
    String frontendUrl;

    @PostMapping("/token")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        var result = authenticationService.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @PostMapping("/google")
    ApiResponse<AuthenticationResponse> authenticateWithGoogle(
            @RequestBody @Valid GoogleAuthenticationRequest request) {
        var result = authenticationService.authenticateWithGoogle(request);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @GetMapping("/google")
    void redirectToGoogle(HttpServletResponse response) throws IOException {
        String state = googleTokenService.generateState();
        Cookie stateCookie = new Cookie(GoogleTokenService.GOOGLE_OAUTH_STATE_COOKIE, state);
        stateCookie.setHttpOnly(true);
        stateCookie.setPath("/");
        stateCookie.setMaxAge(GOOGLE_OAUTH_STATE_MAX_AGE_SECONDS);
        response.addCookie(stateCookie);

        response.sendRedirect(googleTokenService.buildAuthorizationUrl(state));
    }

    @GetMapping("/google/callback")
    void authenticateWithGoogleCallback(
            @RequestParam String code,
            @RequestParam String state,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {
        validateGoogleOAuthState(request, state);
        clearGoogleOAuthStateCookie(response);

        var result = authenticationService.authenticateWithGoogleAuthorizationCode(code);
        response.sendRedirect(buildFrontendRedirectUrl(result.getToken()));
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request)
            throws JOSEException, ParseException {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder().result(result).build();
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> refresh(@RequestBody RefreshRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.refreshToken(request);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    private void validateGoogleOAuthState(HttpServletRequest request, String state) {
        String expectedState = Arrays.stream(request.getCookies() == null ? new Cookie[0] : request.getCookies())
                .filter(cookie -> GoogleTokenService.GOOGLE_OAUTH_STATE_COOKIE.equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        if (!expectedState.equals(state)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    private void clearGoogleOAuthStateCookie(HttpServletResponse response) {
        Cookie stateCookie = new Cookie(GoogleTokenService.GOOGLE_OAUTH_STATE_COOKIE, "");
        stateCookie.setHttpOnly(true);
        stateCookie.setPath("/");
        stateCookie.setMaxAge(0);
        response.addCookie(stateCookie);
    }

    private String buildFrontendRedirectUrl(String token) {
        return UriComponentsBuilder.fromUriString(frontendUrl)
                .queryParam("token", token)
                .build()
                .toUriString();
    }
}
