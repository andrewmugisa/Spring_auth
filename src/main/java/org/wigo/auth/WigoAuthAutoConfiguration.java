package org.wigo.auth;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration entry point for the spring-auth library.
 * Excludes Spring Boot's default security auto-configs to prevent a duplicate
 * filter chain conflict — our SecurityConfiguration owns the filter chain.
 */
@Configuration
@ComponentScan("org.wigo.auth")
@EnableAutoConfiguration(exclude = {
        ServletWebSecurityAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class
})
public class WigoAuthAutoConfiguration {
}
