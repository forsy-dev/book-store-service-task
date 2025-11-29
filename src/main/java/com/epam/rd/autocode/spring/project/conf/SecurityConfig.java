package com.epam.rd.autocode.spring.project.conf;

import com.epam.rd.autocode.spring.project.conf.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig{

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            // *** STATELESS SESSION ***
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // --- Public Endpoints ---
                        // Allow anyone to see the home page, login/register
                        .requestMatchers("/register", "/login", "/logout", "/").permitAll()
                        // Allow access to static resources
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()

                        // --- Employee Endpoints ---
                        // Only employees can add, edit, or delete books
                        .requestMatchers(HttpMethod.PUT, "/profile").hasAnyRole("EMPLOYEE", "CLIENT")
                        .requestMatchers(HttpMethod.PUT, "/clients/profile").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.DELETE, "/clients/profile").hasRole("CLIENT")
                        .requestMatchers(HttpMethod.GET, "/books/new", "/books/*/edit").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.POST, "/books", "/books/**").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.PUT, "/books", "/books/**").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.DELETE, "/books", "/books/**").hasRole("EMPLOYEE")
                        .requestMatchers("/cart/**").hasRole("CLIENT")
                        .requestMatchers("/orders/submit").hasRole("CLIENT")
                        .requestMatchers("/orders/*/cancel", "/orders/*/confirm").hasRole("EMPLOYEE")
                        // Only employees can manage clients
                        .requestMatchers("/clients", "/clients/**", "/employees/**").hasRole("EMPLOYEE")

                        // --- Authenticated Users ---
                        // Any other request must be authenticated.
                        // This now correctly includes GET /books, GET /books/{name},
                        // placing orders, and viewing profiles.
                        .anyRequest().authenticated()
                )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // Disabling CSRF for simplicity in this project

        return http.build();
    }
}
