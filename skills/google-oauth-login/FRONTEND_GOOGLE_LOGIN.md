# Frontend Google Login Integration

Tài liệu này dành cho frontend khi backend tự xử lý Google OAuth Authorization Code flow. Frontend không cần lấy hoặc gửi `idToken`.

## Tổng Quan

Luồng hiện tại:

1. Frontend điều hướng người dùng tới endpoint backend `GET /identity/auth/google`.
2. Backend tạo Google authorization URL, sinh `state`, lưu `state` vào HttpOnly cookie rồi redirect sang Google.
3. Người dùng chọn tài khoản Google.
4. Google redirect về callback backend `GET /identity/auth/google/callback?code=...&state=...`.
5. Backend kiểm tra `state`.
6. Backend dùng `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `GOOGLE_CALLBACK_URL` để đổi `code` lấy Google token.
7. Backend verify Google ID token, tìm hoặc tạo user nội bộ.
8. Backend trả JWT của ứng dụng.

## Biến Môi Trường Backend

Backend đọc các biến môi trường này qua `application-dev.yaml` và `application-prod.yaml`:

```env
GOOGLE_CLIENT_ID=163231380238-bs3urggb8bfiuktcuv17bn94c60t5oq7.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-google-client-secret
GOOGLE_CALLBACK_URL=http://localhost:8081/identity/auth/google/callback
```

Nếu bạn chạy qua gateway và callback thật sự là `/api/auth/google/callback`, có thể đặt:

```env
GOOGLE_CALLBACK_URL=http://localhost:8081/api/auth/google/callback
```

Callback URL phải trùng chính xác với Authorized redirect URI trong Google Cloud Console và phải route được về endpoint backend.

## Endpoint Frontend Cần Gọi

Frontend chỉ cần redirect:

```http
GET /identity/auth/google
```

Ví dụ URL local:

```text
http://localhost:8081/identity/auth/google
```

Backend sẽ tự redirect sang Google.

## Ví Dụ JavaScript Thuần

```js
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

function loginWithGoogle() {
  window.location.href = `${API_BASE_URL}/auth/google`;
}
```

HTML:

```html
<button type="button" onclick="loginWithGoogle()">Continue with Google</button>
```

Với Vite:

```env
VITE_API_BASE_URL=http://localhost:8081/identity
```

## Ví Dụ React

```jsx
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

export default function GoogleLoginButton() {
  const loginWithGoogle = () => {
    window.location.href = `${API_BASE_URL}/auth/google`;
  };

  return (
    <button type="button" onClick={loginWithGoogle}>
      Continue with Google
    </button>
  );
}
```

## Callback Hiện Tại Trả Gì?

Sau khi Google redirect về backend, endpoint callback trả response dạng:

```json
{
  "code": 1000,
  "result": {
    "token": "APP_JWT_TOKEN",
    "authenticated": true
  }
}
```

JWT trong `result.token` là token của ứng dụng, không phải Google token.

## Gọi API Sau Khi Có JWT

Gửi JWT trong header `Authorization`:

```js
async function getMyInfo(token) {
  const response = await fetch(`${API_BASE_URL}/users/my-info`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  return response.json();
}
```

## Lưu Ý Khi Làm UI

Vì callback hiện trả JSON trực tiếp từ backend, trải nghiệm browser sẽ dừng ở trang JSON sau khi đăng nhập thành công. Với sản phẩm thật, nên bổ sung một trong hai cách:

- Backend redirect về frontend success page, ví dụ `/auth/google/success?token=...`.
- Backend set JWT vào HttpOnly cookie rồi redirect về app.

HttpOnly cookie an toàn hơn query token hoặc `localStorage`.

## Lỗi Thường Gặp

- `Unauthenticated`: thiếu env Google, sai client secret, callback URL không khớp, code hết hạn, state sai, hoặc Google email chưa verified.
- Google báo `redirect_uri_mismatch`: `GOOGLE_CALLBACK_URL` không trùng Authorized redirect URI trong Google Cloud Console.
- Không vào được callback: kiểm tra `server.servlet.context-path`. Dự án hiện dùng `/identity`, nên callback local mặc định là `http://localhost:8081/identity/auth/google/callback`.
- CORS không ảnh hưởng tới redirect browser thông thường, nhưng vẫn cần cấu hình CORS cho các API frontend gọi bằng `fetch`.

## Không Cần Làm Ở Frontend

- Không cần nhúng Google Identity Services script.
- Không cần gọi `google.accounts.id.initialize`.
- Không cần gửi `idToken`.
- Không cần giữ Google access token.
- Không đưa `GOOGLE_CLIENT_SECRET` vào frontend.
