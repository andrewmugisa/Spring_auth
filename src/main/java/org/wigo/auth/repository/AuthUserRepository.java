package org.wigo.auth.repository;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

/**
 * Contract that any consuming app must fulfill.
 *
 * In your consuming app (e.g. wigo_events), create a @Repository bean
 * that implements this interface and delegates to your JPA UserRepository.
 *
 * Example:
 *
 *   @Repository
 *   public class AuthUserRepositoryAdapter implements AuthUserRepository {
 *       private final UserRepository jpaRepo;
 *
 *       public AuthUserRepositoryAdapter(UserRepository jpaRepo) {
 *           this.jpaRepo = jpaRepo;
 *       }
 *
 *       @Override public Optional<UserDetails> findByEmail(String email) {
 *           return jpaRepo.findByEmail(email).map(u -> (UserDetails) u);
 *       }
 *
 *       @Override public Optional<UserDetails> findByUsername(String username) {
 *           return jpaRepo.findByUsername(username).map(u -> (UserDetails) u);
 *       }
 *
 *       @Override public Optional<UserDetails> findByVerificationCode(String code) {
 *           return jpaRepo.findByVerificationCode(code).map(u -> (UserDetails) u);
 *       }
 *
 *       @Override public UserDetails save(UserDetails user) {
 *           return jpaRepo.save((UserEntity) user);
 *       }
 *   }
 */
public interface AuthUserRepository {
    Optional<UserDetails> findByEmail(String email);
    Optional<UserDetails> findByUsername(String username);
    Optional<UserDetails> findByVerificationCode(String code);
    UserDetails save(UserDetails user);
}
