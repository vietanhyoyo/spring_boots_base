# Identity Service

REST API quản lý người dùng, đăng nhập JWT, đăng ký thường, đăng ký Google và phân quyền theo `Role - Resource - Permission`.

## Công nghệ

- Java 17
- Spring Boot 3.3.4
- Spring Web
- Spring Security OAuth2 Resource Server
- Spring Data JPA, Hibernate
- MySQL
- Maven Wrapper
- MapStruct
- Lombok

## Cấu Trúc Source

```text
src/main/java/com/spring/identity
├── configuration
│   ├── SecurityConfig.java
│   ├── CustomJwtDecoder.java
│   ├── JwtAuthenticationEntryPoint.java
│   └── seed
│       ├── PermissionSeedConfig.java
│       ├── ResourceSeedConfig.java
│       ├── RoleSeedConfig.java
│       ├── AdminRolePermissionSeedConfig.java
│       └── AdminUserSeedConfig.java
├── controller
│   ├── AuthenticationController.java
│   ├── RegisterController.java
│   ├── UserController.java
│   ├── RoleController.java
│   └── PermissionController.java
├── dto
│   ├── request
│   └── response
├── entity
│   ├── User.java
│   ├── Role.java
│   ├── Permission.java
│   ├── Resource.java
│   ├── RoleResourcePermission.java
│   └── InvalidatedToken.java
├── enums
│   ├── Role.java
│   ├── Permission.java
│   └── Resource.java
├── repository
├── service
├── mapper
├── exception
└── validator
```

## Mô Hình Chính

`User` có nhiều `Role`.

`Role` được phân quyền theo từng `Resource` và từng `Permission` thông qua bảng trung gian `RoleResourcePermission`.

```text
User
  └── roles
        └── Role
              └── RoleResourcePermission
                    ├── Resource
                    └── Permission
```

Ví dụ:

```text
ADMIN - USER_MANAGEMENT - VIEW
ADMIN - USER_MANAGEMENT - CREATE
ADMIN - USER_MANAGEMENT - UPDATE
ADMIN - USER_MANAGEMENT - DELETE
ADMIN - USER_MANAGEMENT - EXPORT
```

Các `Permission` mặc định:

```text
VIEW, CREATE, UPDATE, DELETE, EXPORT
```

Các `Resource` mặc định:

```text
USER_MANAGEMENT, ROLE_MANAGEMENT, PERMISSION_MANAGEMENT, REPORT
```

## Seed Data

Khi app start với MySQL, các seeder trong `configuration/seed` sẽ tự chạy:

- Seed bảng `permission` nếu rỗng.
- Seed bảng `resource` nếu rỗng.
- Seed bảng `role` nếu rỗng.
- Gán role `ADMIN` đủ mọi quyền trên mọi resource.
- Tạo user admin mặc định nếu chưa có:

```text
email: admin@gmail.com
password: 12345678
```

## Cấu Hình

File mặc định:

```text
src/main/resources/application.yaml
```

App đang dùng profile mặc định:

```yaml
spring:
  profiles:
    active: dev
```

Base URL có context path:

```text
http://localhost:8081/identity
```

Dev database nằm trong:

```text
src/main/resources/application-dev.yaml
```

Mặc định:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/identity_service
    username: root
    password: root
  jpa:
    hibernate:
      ddl-auto: update
```

## API Chính

Public API:

```http
POST /identity/register
POST /identity/register/google
POST /identity/auth/token
POST /identity/auth/introspect
POST /identity/auth/refresh
POST /identity/auth/logout
```

Protected API:

```http
GET    /identity/users
POST   /identity/users
GET    /identity/users/{userId}
PUT    /identity/users/{userId}
DELETE /identity/users/{userId}
GET    /identity/users/myInfo

GET    /identity/roles
POST   /identity/roles
DELETE /identity/roles/{role}

GET    /identity/permissions
POST   /identity/permissions
DELETE /identity/permissions/{permission}
```

Các protected API cần header:

```http
Authorization: Bearer <token>
```

## Ví Dụ Request

Đăng ký thường:

```bash
curl --location 'http://localhost:8081/identity/register' \
--header 'Content-Type: application/json' \
--data-raw '{
  "username": "testuser",
  "email": "testuser@example.com",
  "password": "12345678",
  "firstName": "Test",
  "lastName": "User",
  "dob": "2000-01-01"
}'
```

Đăng nhập bằng email:

```bash
curl --location 'http://localhost:8081/identity/auth/token' \
--header 'Content-Type: application/json' \
--data-raw '{
  "email": "admin@gmail.com",
  "password": "12345678"
}'
```

Đăng ký bằng Google:

```bash
curl --location 'http://localhost:8081/identity/register/google' \
--header 'Content-Type: application/json' \
--data-raw '{
  "idToken": "GOOGLE_ID_TOKEN_FROM_FRONTEND"
}'
```

Tạo role với quyền theo resource:

```bash
curl --location 'http://localhost:8081/identity/roles' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer <token>' \
--data-raw '{
  "name": "MANAGER",
  "description": "Manager role",
  "resourcePermissions": [
    {
      "resource": "USER_MANAGEMENT",
      "permissions": ["VIEW", "CREATE", "UPDATE"]
    },
    {
      "resource": "REPORT",
      "permissions": ["VIEW", "EXPORT"]
    }
  ]
}'
```

## Cách Chạy Trên VS Code

### 1. Cài extension cần thiết

Trong VS Code, cài:

- Extension Pack for Java
- Spring Boot Extension Pack
- Lombok Annotations Support for VS Code

### 2. Chuẩn bị Java

Kiểm tra Java 17:

```bash
java -version
```

Nếu VS Code chưa nhận đúng Java, mở Command Palette:

```text
Java: Configure Java Runtime
```

Chọn JDK 17.

### 3. Chuẩn bị MySQL

Tạo database:

```sql
CREATE DATABASE identity_service;
```

Đảm bảo thông tin trong `application-dev.yaml` khớp với MySQL local:

```yaml
username: root
password: root
```

### 4. Chạy app

Cách 1: chạy bằng terminal trong VS Code:

```bash
./mvnw spring-boot:run
```

Cách 2: chạy bằng Spring Boot Dashboard:

```text
Spring Boot Dashboard -> demo-spring -> Run
```

App chạy tại:

```text
http://localhost:8081/identity
```

### 5. Debug app

Trong VS Code:

1. Mở file `DemoSpringApplication.java`.
2. Bấm `Run` hoặc `Debug` phía trên method `main`.
3. Đặt breakpoint trong controller hoặc service cần kiểm tra.
4. Gọi API bằng Postman/curl.

## Build Và Test

Build nhanh, bỏ qua test:

```bash
./mvnw -DskipTests package
```

Chạy test:

```bash
./mvnw test
```

Lưu ý: một số test hiện dùng Testcontainers, nên cần Docker đang chạy.
