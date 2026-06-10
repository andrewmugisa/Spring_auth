package org.wigo.auth.model;

/**
 * Factory interface the consuming app must implement to create new users.
 *
 * Example in wigo_events:
 *
 *   @Component
 *   public class UserEntityFactory implements AuthUserFactory {
 *       @Override
 *       public AuthUser create(String username, String email, String password, String name) {
 *           return new UserEntity(username, email, password, name);
 *       }
 *   }
 */
public interface AuthUserFactory {
    AuthUser create(String username, String email, String password, String name);
}
