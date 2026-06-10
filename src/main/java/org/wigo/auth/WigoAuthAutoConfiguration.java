package org.wigo.auth;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration entry point for the spring-auth library.
 * Spring Boot picks this up automatically via:
 *   META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
 */
@Configuration
@ComponentScan("org.wigo.auth")
public class WigoAuthAutoConfiguration {
}
