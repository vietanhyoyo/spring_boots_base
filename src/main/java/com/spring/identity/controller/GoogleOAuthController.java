package com.spring.identity.controller;

import java.io.IOException;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spring.identity.service.GoogleTokenService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GoogleOAuthController {
    static int GOOGLE_OAUTH_STATE_MAX_AGE_SECONDS = 300;

    GoogleTokenService googleTokenService;

    @GetMapping("/oauth2/authorization/google")
    void redirectToGoogle(HttpServletResponse response) throws IOException {
        String state = googleTokenService.generateState();
        Cookie stateCookie = new Cookie(GoogleTokenService.GOOGLE_OAUTH_STATE_COOKIE, state);
        stateCookie.setHttpOnly(true);
        stateCookie.setPath("/");
        stateCookie.setMaxAge(GOOGLE_OAUTH_STATE_MAX_AGE_SECONDS);
        response.addCookie(stateCookie);

        response.sendRedirect(googleTokenService.buildAuthorizationUrl(state));
    }
}
