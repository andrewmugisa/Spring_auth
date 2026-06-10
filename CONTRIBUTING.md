# Contributing to spring-auth

## How to make changes

1. Make your fix or improvement in `src/main/java/org/wigo/auth/`
2. Bump the version in `pom.xml` (e.g. `1.0.0` → `1.0.1`)
3. Run `./mvnw clean install -DskipTests` to install locally
4. Test in a consuming app (e.g. `wigo_events`) before pushing
5. Push and update any consuming apps to the new version

## Version bumping convention

- `1.0.x` — bug fixes, no breaking changes
- `1.x.0` — new features added (e.g. forgot password, OAuth)
- `x.0.0` — breaking changes (consuming apps need code changes)

## Adding a new auth feature

All auth features belong here, not in consuming apps. Examples:
- Forgot password / password reset
- Change password
- Change email
- Refresh tokens
- Google OAuth / Sign in with Google

Each feature should follow the existing pattern:
- DTO in `dto/`
- Logic in `service/`
- Endpoint in `controller/AuthController.java`
- Error handling via the existing `GlobalExceptionHandler`
