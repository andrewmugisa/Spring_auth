# spring-auth

A reusable Spring Boot authentication library for Wigo projects.

Drop it into any Spring Boot app and get a fully working auth system — JWT login, email verification, token blacklisting, and security config — without writing any of it again.

---

## What this library gives you

When you add `spring-auth` as a dependency, your app automatically gets:

| Feature | Details |
|---|---|
| `POST /auth/signup` | Register with name, email, password. Sends verification email. |
| `POST /auth/login` | Login by email + password. Returns JWT token. |
| `POST /auth/verify` | Verify account with 6-digit code from email. |
| `POST /auth/resend` | Resend verification code. |
| `POST /auth/logout` | Blacklists the token server-side. |
| JWT filter | Every request validated automatically. |
| Security config | Stateless sessions, CORS, public/private routes. |
| Global exception handler | Consistent error responses across all endpoints. |
| Email service | HTML verification emails via SMTP. |

Your app only needs to own its **database**, its **User entity**, and any **app-specific business logic**.

---

## What you are responsible for (in your app)

The library is auth-only. Everything else stays in your app:

| Your app owns | Why |
|---|---|
| `UserEntity` | Extends `AuthUser` — you add your own fields here |
| `UserRepository` | JPA repo — you own the database |
| `AuthUserRepositoryAdapter` | 10-line bridge connecting your repo to the library |
| `UserEntityFactory` | Tells the library how to create your `UserEntity` |
| All other tables/entities | Events, bookings, products — none of that is touched |
| `application.properties` | DB config, mail config, JWT secret — stays in your app |

---

## Database — important

This library does **not** create or manage a separate database.

It uses **your app's database**. The `users` table lives in your app's DB alongside all your other tables (events, bookings, etc.). The library just needs a `UserEntity` that extends `AuthUser` — the base class provides the auth-related columns (`email`, `password`, `verification_code`, `enabled`, etc.).

So if your app has:
```
users         ← auth columns come from AuthUser base class
events        ← your table
bookings      ← your table
```
Everything is in one database. You add tables freely — the library doesn't care.

---

## How to use in a new project

### Step 1 — Install the library

```bash
cd ~/IdeaProjects/Spring_auth
./mvnw clean install
```

This installs it to your local Maven repo (`~/.m2`).

### Step 2 — Add the dependency

In your app's `pom.xml`:

```xml
<dependency>
    <groupId>org.wigo</groupId>
    <artifactId>spring-auth</artifactId>
    <version>1.0.0</version>
</dependency>
```

Remove any deps your app no longer needs directly: `spring-boot-starter-security`, `spring-boot-starter-mail`, `jjwt-*`. They are pulled in transitively by the library.

### Step 3 — Create your UserEntity

```java
@Entity
@Table(name = "users")
public class UserEntity extends AuthUser {

    // Add your app-specific fields here
    // e.g. private String profilePicture;
    // e.g. @OneToMany private List<Event> events;

    public UserEntity(String username, String email, String password, String name) {
        super(username, email, password, name);
    }

    public UserEntity() { super(); }
}
```

### Step 4 — Add the two adapter classes

**`AuthUserRepositoryAdapter.java`** — bridges your JPA repo to the library:

```java
@Repository
public class AuthUserRepositoryAdapter implements AuthUserRepository {

    private final UserRepository userRepository;

    public AuthUserRepositoryAdapter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override public Optional<UserDetails> findByEmail(String email) {
        return userRepository.findByEmail(email).map(u -> (UserDetails) u);
    }
    @Override public Optional<UserDetails> findByUsername(String username) {
        return userRepository.findByUsername(username).map(u -> (UserDetails) u);
    }
    @Override public Optional<UserDetails> findByVerificationCode(String code) {
        return userRepository.findByVerificationCode(code).map(u -> (UserDetails) u);
    }
    @Override public UserDetails save(UserDetails user) {
        return userRepository.save((UserEntity) user);
    }
}
```

**`UserEntityFactory.java`** — tells the library how to instantiate your entity:

```java
@Component
public class UserEntityFactory implements AuthUserFactory {
    @Override
    public AuthUser create(String username, String email, String password, String name) {
        return new UserEntity(username, email, password, name);
    }
}
```

### Step 5 — Add required properties

In your `application.properties` or `.env`:

```properties
# JWT
security.jwt.secret-key=your-base64-secret
security.jwt.expiration-time=3600000

# Mail (SMTP)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.starttls.enable=true

# CORS
server.localhost-url=http://127.0.0.1:5500
server.local-test-url=http://localhost:3000
```

That's it. Start your app — all `/auth/**` endpoints are live.

---

## Updating the library

When you fix or improve authentication in `Spring_auth`:

```bash
# 1. Make your changes in Spring_auth
# 2. Bump the version in Spring_auth pom.xml (e.g. 1.0.0 → 1.0.1)
cd ~/IdeaProjects/Spring_auth
./mvnw clean install

# 3. Update the version in your app's pom.xml
# 4. Rebuild your app
cd ~/IdeaProjects/wigo_events
./mvnw clean compile
```

Every project that depends on `spring-auth` gets the fix by bumping one version number.

---

## Project structure reference

```
spring-auth/
└── src/main/java/org/wigo/auth/
    ├── WigoAuthAutoConfiguration.java   ← Spring Boot auto-wires everything
    ├── config/
    │   ├── SecurityConfiguration.java   ← JWT filter chain, CORS, public routes
    │   ├── ApplicationConfiguration.java← UserDetailsService, BCrypt, AuthManager
    │   ├── JwtAuthenticationFilter.java ← validates Bearer token on every request
    │   ├── EmailConfiguration.java      ← JavaMailSender setup
    │   └── GlobalExceptionHandler.java  ← consistent error response format
    ├── controller/
    │   └── AuthController.java          ← /auth/* endpoints
    ├── service/
    │   ├── AuthenticationService.java   ← register, login, verify, resend logic
    │   ├── JwtService.java              ← token generation and validation
    │   ├── EmailService.java            ← sends HTML emails
    │   └── TokenBlacklistService.java   ← logout / token revocation
    ├── model/
    │   ├── AuthUser.java                ← abstract base entity (extend this)
    │   └── AuthUserFactory.java         ← interface your app implements
    ├── repository/
    │   └── AuthUserRepository.java      ← interface your app implements
    ├── dto/                             ← RegisterUserDto, LoginUserDto, etc.
    └── response/                        ← LoginResponse, ApiResponse
```
