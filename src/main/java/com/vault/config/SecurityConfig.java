package com.vault.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import com.vault.service.CustomUserDetailsService;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Security configuration for handling user authentication and authorization.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * Defines the password encoding mechanism using BCrypt for secure password storage.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the authentication provider with custom user details service and password encoder.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Configures the authentication manager, managing user authentication for the application.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Defines the security filter chain, establishing access rules for specific endpoints.
     * Enables HTTP Basic authentication for secured routes.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disables CSRF protection for stateless APIs
            .csrf(csrf -> csrf.disable())
            // Configures endpoint access rules
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/files/**").authenticated()
                .anyRequest().permitAll()
            )
            // Enables basic authentication for API access
            .httpBasic(withDefaults()); // Replaces .httpBasic() for non-deprecated alternative

        return http.build();
    }
}
