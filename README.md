---

# Auth Layer — Manual Test Guide (Final)

---

## 1. Signup — Happy Path

**Endpoint:** `POST /auth/signup`

**Request:**

```json
{
  "email": "testEmail@test.com",
  "password": "123456",
  "name": "Test User"
}
```

**Expected 200**

```json
{
  "userId": 1,
  "username": "testuser_4821",  // auto-generated display username
  "name": "Test User",
  "email": "testEmail@test.com",
  "enabled": false,
  "createdAt": "2026-05-10T00:00:00Z"
}
```

✅ No `password`, `verificationCode`, `verificationCodeExpiration` in response
✅ `username` is auto-generated
✅ `enabled` is `false` until verification

---

## 2. Signup — Validation Errors

**Request:**

```json
{
  "email": "not-an-email",
  "password": "123",
  "name": ""
}
```

**Expected 400**

```json
{
  "timestamp": "...",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "details": {
    "email": "Invalid email format",
    "password": "Password must be at least 6 characters",
    "name": "Name is required"
  }
}
```

---

## 3. Signup — Duplicate Email

**Request:** *(same email as test 1)*

```json
{
  "email": "testEmail@test.com",
  "password": "123456",
  "name": "Test Two"
}
```

**Expected 400**

```json
{
  "timestamp": "...",
  "status": 400,
  "error": "Bad Request",
  "message": "Email is already registered"
}
```

---

## 4. Signup — Name Collision

* Register two users with the same `name`.
* Second signup should get a **unique auto-generated username**.

```json
{ "email": "user1@gmail.com", "password": "123456", "name": "John Doe" }
```

→ `username`: `johndoe`

```json
{ "email": "user2@gmail.com", "password": "123456", "name": "John Doe" }
```

→ `username`: `johndoe_XXXX`

✅ Uniqueness handled server-side; no conflict errors

---

## 5. Login — Unverified Account

**Request:**

```json
{
  "email": "testEmail@test.com",
  "password": "123456"
}
```

**Expected 403**

```json
{
  "timestamp": "...",
  "status": 403,
  "error": "Forbidden",
  "message": "Account not verified. Please check your email"
}
```

---

## 6–9. Verify Account

### 6. Wrong Email

```json
{
  "email": "wrong@gmail.com",
  "verificationCode": "123456"
}
```

**Expected 400**

```json
{
  "timestamp": "...",
  "status": 400,
  "error": "Bad Request",
  "message": "User not found"
}
```

### 7. Wrong Code

```json
{
  "email": "testEmail@test.com",
  "verificationCode": "000000"
}
```

**Expected 400**

```json
{
  "timestamp": "...",
  "status": 400,
  "error": "Bad Request",
  "message": "Verification code does not match"
}
```

### 8. Happy Path

```json
{
  "email": "testEmail@test.com",
  "verificationCode": "XXXXXX" // use actual code from email
}
```

**Expected 200**

```json
{
  "message": "Account verified successfully"
}
```

### 9. Already Verified

```json
{
  "email": "testEmail@test.com",
  "verificationCode": "XXXXXX"
}
```

**Expected 409**

```json
{
  "timestamp": "...",
  "status": 409,
  "error": "Conflict",
  "message": "Account is already verified"
}
```

---

## 10–12. Resend Verification Code

### 10. User Not Found

```json
{ "email": "nobody@gmail.com" }
```

**Expected 400**

```json
{
  "timestamp": "...",
  "status": 400,
  "error": "Bad Request",
  "message": "User not found"
}
```

### 11. Already Verified

```json
{ "email": "testEmail@test.com" }
```

**Expected 409**

```json
{
  "timestamp": "...",
  "status": 409,
  "error": "Conflict",
  "message": "Account is already verified"
}
```

### 12. Happy Path

* Sign up a fresh account (unverified)
* Send:

```json
{ "email": "fresh@gmail.com" }
```

**Expected 200**

```json
{
  "message": "Verification code resent successfully"
}
```

✅ Old code invalidated

---

## 13–14. Login

### 13. Wrong Password

```json
{
  "email": "testEmail@test.com",
  "password": "wrongpassword"
}
```

**Expected 401**

```json
{
  "timestamp": "...",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password"
}
```

### 14. Happy Path

```json
{
  "email": "testEmail@test.com",
  "password": "123456"
}
```

**Expected 200**

```json
{
  "token": "eyJ...",
  "expiresIn": 3600000
}
```

✅ Save token for authenticated endpoints

---

## 15–17. Get Current User

**Endpoint:** `GET /users/me`
**Header:** `Authorization: Bearer <token>`

* **15. Valid token → 200**
* **16. No token → 401**
* **17. Invalid token → 401**

✅ Response excludes sensitive fields

---

## 18–20. Update Username

**Endpoint:** `PATCH /users/me/username`
**Header:** `Authorization: Bearer <token>`

### 18. Happy Path

```json
{ "username": "mynewusername" }
```

**Expected 200**

```json
{
  "userId": 1,
  "username": "mynewusername",
  "name": "Test User",
  "email": "testEmail@test.com",
  "enabled": true,
  "createdAt": "2026-05-10T00:00:00Z"
}
```

### 19. Username Taken

```json
{ "username": "mynewusername" }
```

**Expected 400**

```json
{
  "timestamp": "...",
  "status": 400,
  "error": "Bad Request",
  "message": "Username is already taken"
}
```

### 20. No Token → 401

---

## 21. Expired Verification Code

* Temporarily set expiry to 1 minute for testing:

```java
private static final int VERIFICATION_EXPIRY_MINUTES = 1;
```

* Sign up → wait → verify

**Expected 409**

```json
{
  "timestamp": "...",
  "status": 409,
  "error": "Conflict",
  "message": "Verification code has expired. Please request a new one"
}
```

---

### ✅ Pass Criteria Summary

| #  | Endpoint          | Scenario                 | Status |
| -- | ----------------- | ------------------------ | ------ |
| 1  | POST /auth/signup | Happy path               | 200    |
| 2  | POST /auth/signup | Validation errors        | 400    |
| 3  | POST /auth/signup | Duplicate email          | 400    |
| 4  | POST /auth/signup | Duplicate name collision | 200    |
| 5  | POST /auth/login  | Unverified account       | 403    |
| 6  | POST /auth/verify | Wrong email              | 400    |
| 7  | POST /auth/verify | Wrong code               | 400    |
| 8  | POST /auth/verify | Happy path               | 200    |
| 9  | POST /auth/verify | Already verified         | 409    |
| 10 | POST /auth/resend | User not found           | 400    |
| 11 | POST /auth/resend | Already verified         | 409    |
| 12 | POST /auth/resend | Happy path               | 200    |
| 13 | POST /auth/login  | Wrong password           | 401    |
| 14 | POST /auth/login  | Happy path               | 200    |
| 15 | GET /users/me     | Valid token              | 200    |
| 16 | GET /users/me     |                          |        |
