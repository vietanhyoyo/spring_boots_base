package com.spring.identity.service;

import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import com.spring.identity.dto.response.GoogleTokenExchangeResponse;
import com.spring.identity.dto.response.GoogleTokenInfoResponse;
import com.spring.identity.exception.AppException;
import com.spring.identity.exception.ErrorCode;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GoogleTokenService {

    @NonFinal
    @Value("${google.client-id:}")
    String googleClientId;

    @NonFinal
    @Value("${google.client-secret:}")
    String googleClientSecret;

    @NonFinal
    @Value("${google.callback-url:}")
    String googleCallbackUrl;

    static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com";
    public static final String GOOGLE_OAUTH_STATE_COOKIE = "GOOGLE_OAUTH_STATE";

    public String generateState() {
        return UUID.randomUUID().toString();
    }

    public String buildAuthorizationUrl(String state) {
        if (!StringUtils.hasText(googleClientId)
                || !StringUtils.hasText(googleCallbackUrl)
                || !StringUtils.hasText(state)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return UriComponentsBuilder.fromUriString(GOOGLE_AUTH_URL)
                .queryParam("client_id", googleClientId)
                .queryParam("redirect_uri", googleCallbackUrl)
                .queryParam("response_type", "code")
                .queryParam("scope", "openid email profile")
                .queryParam("state", state)
                .build()
                .toUriString();
    }

    public GoogleTokenInfoResponse exchangeAuthorizationCode(String code) {
        if (!StringUtils.hasText(code)
                || !StringUtils.hasText(googleClientId)
                || !StringUtils.hasText(googleClientSecret)
                || !StringUtils.hasText(googleCallbackUrl)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        var requestBody = new LinkedMultiValueMap<String, String>();
        requestBody.add("code", code);
        requestBody.add("client_id", googleClientId);
        requestBody.add("client_secret", googleClientSecret);
        requestBody.add("redirect_uri", googleCallbackUrl);
        requestBody.add("grant_type", "authorization_code");

        try {
            GoogleTokenExchangeResponse tokenResponse = RestClient.create(GOOGLE_TOKEN_URL)
                    .post()
                    .uri("/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(requestBody)
                    .retrieve()
                    .body(GoogleTokenExchangeResponse.class);

            if (tokenResponse == null || !StringUtils.hasText(tokenResponse.getIdToken())) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }

            return verifyGoogleIdToken(tokenResponse.getIdToken());
        } catch (RestClientException exception) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    public GoogleTokenInfoResponse verifyGoogleIdToken(String idToken) {
        if (!StringUtils.hasText(idToken) || !StringUtils.hasText(googleClientId)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        try {
            GoogleTokenInfoResponse tokenInfo = RestClient.create(GOOGLE_TOKEN_URL)
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/tokeninfo")
                            .queryParam("id_token", idToken)
                            .build())
                    .retrieve()
                    .body(GoogleTokenInfoResponse.class);

            if (tokenInfo == null
                    || !googleClientId.equals(tokenInfo.getAud())
                    || !isValidIssuer(tokenInfo.getIss())
                    || !"true".equalsIgnoreCase(tokenInfo.getEmailVerified())
                    || !StringUtils.hasText(tokenInfo.getEmail())) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }

            tokenInfo.setEmail(tokenInfo.getEmail().trim().toLowerCase(Locale.ROOT));

            return tokenInfo;
        } catch (RestClientException exception) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    private boolean isValidIssuer(String issuer) {
        return "accounts.google.com".equals(issuer) || "https://accounts.google.com".equals(issuer);
    }
}
