package org.wigo.auth.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

/**
 * Base user class for the auth library.
 *
 * Your consuming app's UserEntity should EXTEND this class and add
 * any app-specific fields (e.g. profilePicture, role, events, etc.)
 *
 * Example in wigo_events:
 *
 *   @Entity
 *   @Table(name = "users")
 *   public class UserEntity extends AuthUser {
 *       // add your wigo_events-specific fields here
 *       private String profilePicture;
 *       // ...
 *   }
 */
@Getter
@Setter
@MappedSuperclass
public abstract class AuthUser implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(unique = true, nullable = false)
    private String username; // display username

    @Column(unique = true, nullable = false, length = 100)
    private String email; // used for Spring Security login

    @Column(nullable = false)
    private String password;

    private String name;

    private boolean enabled;

    @Column(name = "verification_code")
    private String verificationCode;

    @Column(name = "verification_expiration")
    private Instant verificationCodeExpiration;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    public AuthUser(String username, String email, String password, String name) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.name = name;
    }

    public AuthUser() {}

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    /**
     * Spring Security uses this for login — keep it as email.
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * The display username for API responses.
     */
    public String getUserDisplayName() {
        return username;
    }

    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return enabled; }
}
