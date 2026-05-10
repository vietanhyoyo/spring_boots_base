---
name: google-oauth-login
description: Use this skill when implementing, reviewing, or explaining Google login and Google sign-up in any backend/frontend stack. It covers the language-agnostic authorization-code flow, callback handling, Google identity verification, find-or-create user logic, frontend success handoff, security checks, and common environment configuration.
---

# Google OAuth Login

Dùng skill này khi cần thiết kế, giải thích, review hoặc triển khai đăng nhập và đăng ký bằng Google cho bất kỳ ngôn ngữ/framework nào. Mặc định dùng OAuth 2.0 Authorization Code flow. Với mobile app hoặc SPA tự đổi `code`, dùng thêm PKCE. Với backend giữ vai trò xử lý login/register, backend có thể đổi `code` bằng `client_secret`.

## Luồng Chính

1. Người dùng bấm "Continue with Google" ở frontend.
2. Frontend điều hướng sang endpoint backend, ví dụ `GET /auth/google`.
3. Backend tạo Google authorization URL gồm:
   - `client_id`
   - `redirect_uri`
   - `response_type=code`
   - `scope=openid email profile`
   - `state` là chuỗi ngẫu nhiên để chống CSRF
   - `code_challenge` và `code_challenge_method=S256` nếu dùng PKCE
4. Backend lưu `state` và, nếu có PKCE, lưu `code_verifier` trong server-side store ngắn hạn hoặc secure same-site cookie.
5. Backend redirect trình duyệt sang Google.
6. Google xác thực người dùng rồi redirect về callback, ví dụ `GET /auth/google/callback?code=...&state=...`.
7. Backend kiểm tra `state`.
8. Backend đổi `code` lấy Google tokens.
9. Backend verify Google ID token hoặc gọi Google userinfo endpoint.
10. Backend tìm user nội bộ từ danh tính Google đã xác thực.
11. Nếu user đã tồn tại, đây là luồng đăng nhập bằng Google.
12. Nếu user chưa tồn tại, backend tạo user mới, đây là luồng đăng ký bằng Google.
13. Backend tạo trạng thái đăng nhập của app, ví dụ session cookie hoặc access token nội bộ.
14. Backend redirect người dùng về frontend success page hoặc set cookie rồi redirect vào app.

## Endpoint Tối Thiểu

Backend thường cần:

- `GET /auth/google`: bắt đầu login và redirect sang Google.
- `GET /auth/google/callback`: nhận callback từ Google, tìm user để đăng nhập hoặc tạo user mới để đăng ký.

Frontend thường không cần giữ Google access token, trừ khi app thật sự gọi Google API thay mặt người dùng. Đa số trường hợp chỉ cần session hoặc JWT của chính ứng dụng.

## Cấu Hình Provider

Tạo OAuth client trong Google Cloud Console và cấu hình:

- Authorized redirect URI trùng chính xác callback backend, ví dụ `https://api.example.com/auth/google/callback`.
- Redirect URI local để dev, ví dụ `http://localhost:3000/auth/google/callback`.
- Scopes khởi đầu: `openid email profile`.

Biến môi trường thường cần:

```text
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
GOOGLE_CALLBACK_URL=
FRONTEND_URL=
APP_JWT_SECRET= hoặc SESSION_SECRET=
```

Không commit client secret hoặc cấu hình production nhạy cảm vào source code.

## Thuật Toán Callback

Dùng khung trung lập ngôn ngữ này:

```text
callback(request):
  code = request.query["code"]
  returnedState = request.query["state"]

  expectedState = loadStateFromCookieOrStore(request)
  assert constantTimeEqual(returnedState, expectedState)

  tokenResponse = exchangeAuthorizationCode(
    code,
    clientId,
    clientSecret,
    redirectUri,
    optionalCodeVerifier
  )

  idTokenClaims = verifyIdToken(tokenResponse.id_token, expectedAudience=clientId)
  assert idTokenClaims.iss is "https://accounts.google.com" or "accounts.google.com"
  assert idTokenClaims.email_verified is true

  googleUserId = idTokenClaims.sub
  email = normalizeEmail(idTokenClaims.email)
  name = idTokenClaims.name or idTokenClaims.given_name or email
  avatarUrl = idTokenClaims.picture

  user = findUserByProvider("google", googleUserId)
  if user does not exist:
    user = findUserByEmail(email)

  if user exists:
    # Login with Google
    linkGoogleIdentityIfMissing(user, googleUserId)
  else:
    # Register with Google
    user = createUser(
      email=email,
      displayName=name,
      avatarUrl=avatarUrl,
      provider="google",
      providerUserId=googleUserId,
      password=null,
      status="enabled",
      defaultRole="user"
    )

  loginState = createAppLoginState(user)
  clearOAuthState()
  redirectToFrontendSuccess(loginState)
```

## Data Model

Tối thiểu nên lưu:

- `users.email`, unique nếu email là khoá tài khoản chính.
- `users.password_hash`, nullable cho tài khoản chỉ đăng nhập social.
- `users.status`, ví dụ enabled hoặc disabled.
- `oauth_accounts.provider`, ví dụ `google`.
- `oauth_accounts.provider_user_id`, chính là claim `sub` ổn định của Google.
- `oauth_accounts.user_id`, liên kết về user nội bộ.

Với dự án nhỏ, có thể lưu trực tiếp `google_id` trên bảng `users`. Tuy vậy, bảng `oauth_accounts` riêng sẽ dễ mở rộng sang Facebook, Apple, GitHub, Zalo hoặc nhiều tài khoản Google.

## Bàn Giao Đăng Nhập Cho Frontend

Ưu tiên HttpOnly cookie:

- Backend set `Set-Cookie` cho session hoặc token đăng nhập nội bộ.
- Cookie flags: `HttpOnly`, `Secure` ở production, `SameSite=Lax` hoặc `Strict`.
- Backend redirect về `FRONTEND_URL/auth/sign-in/success`.

Nếu app hiện dùng query token, redirect dạng:

```text
FRONTEND_URL/auth/sign-in/success?access_token=...
```

Cách này đơn giản nhưng kém an toàn hơn vì query string có thể lọt vào browser history, logs, referrers hoặc analytics. Khi có thể, chuyển sang cookie hoặc one-time login code để frontend đổi lấy trạng thái đăng nhập.

## Checklist Bảo Mật

- Luôn validate `state` ở callback.
- Dùng PKCE cho public client; cân nhắc dùng cho mọi flow.
- Verify chữ ký, issuer, audience, expiration của ID token; verify nonce nếu có dùng.
- Chỉ tin email khi `email_verified=true`.
- Dùng `sub` của Google làm định danh provider ổn định; email có thể thay đổi.
- Không nhận profile do frontend gửi lên như bằng chứng đã đăng nhập.
- Không đưa Google client secret vào frontend.
- OAuth state nên sống ngắn, thường 5 đến 10 phút.
- Xử lý linking khi trùng email một cách chủ động.
- Chặn user nội bộ đã bị disabled dù Google xác thực thành công.
- Log lỗi đủ để debug nhưng không log raw token hoặc secret.

## Xử Lý Lỗi

Cần xử lý rõ:

- Người dùng từ chối consent: redirect về frontend error page.
- Thiếu `code` hoặc `state`: reject callback.
- `state` sai hoặc hết hạn: reject callback.
- Đổi code thất bại: redirect về trang lỗi an toàn.
- Google không trả email hoặc email chưa verified: reject hoặc yêu cầu phương thức đăng nhập khác.
- Email đã thuộc tài khoản password login: link sau khi xác minh hoặc yêu cầu đăng nhập tài khoản cũ trước.
- User nội bộ đang disabled: reject login.

## Cách Áp Dụng Cho Bất Kỳ Stack Nào

Với bất kỳ ngôn ngữ nào, map luồng trên vào các primitive này:

- Route HTTP redirect cho `/auth/google`.
- Route HTTP callback cho `/auth/google/callback`.
- OAuth client library hoặc HTTP call thủ công tới Google token endpoint.
- JWT/OIDC library để verify ID token.
- Database lookup và transaction để find-or-create user.
- Tạo trạng thái đăng nhập theo auth system sẵn có của app.

Ví dụ theo stack:

- Node/NestJS/Express: route guard hoặc controller redirect, strategy validate profile, service tạo app token.
- Laravel/Symfony: controller redirect qua OAuth client, callback tạo user và login session.
- Django/FastAPI/Flask: view bắt đầu OAuth, callback đổi code, dùng session hoặc JWT.
- Spring Boot: OAuth2 login filter xử lý callback, success handler link hoặc tạo local account.
- Go/Rust: dùng OAuth2 và OIDC library, sau đó issue app cookie hoặc JWT.

## Pattern Hiện Tại Của CanThoHub

Repo hiện tại đang đi theo mẫu NestJS:

- `GET /auth/google` dùng Google strategy để redirect trình duyệt sang Google.
- `GET /auth/google/callback` nhận `req.user` từ strategy.
- Strategy request scope `email` và `profile`.
- Auth service tìm user theo email hoặc tạo user mới với trạng thái enabled, role mặc định và `password=null`.
- Nếu user tồn tại, service xem đây là đăng nhập bằng Google.
- Nếu user chưa tồn tại, service xem đây là đăng ký bằng Google.
- Auth service tạo trạng thái đăng nhập nội bộ cho app.
- Callback redirect về `FRONTEND_URL/auth/sign-in/success`.

Khi mang pattern này sang dự án khác, giữ lại hành vi nghiệp vụ nhưng nên bổ sung `state`, verify ID token, lưu `sub` của Google, và ưu tiên bàn giao token bằng HttpOnly cookie nếu kiến trúc cho phép.
