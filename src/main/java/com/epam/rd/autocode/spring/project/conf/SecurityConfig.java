package com.epam.rd.autocode.spring.project.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
public class SecurityConfig{

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        // --- Public Endpoints ---
                        // Allow anyone to see the home page, login/register
                        .requestMatchers("/register").permitAll()
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
                .formLogin(form -> form
                        .loginPage("/login") // Specify custom login page
                        .defaultSuccessUrl("/books", true)
                        .permitAll() // Allow everyone to see the login page
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login") // Redirect to home page on logout
                        .permitAll()
                )
                .sessionManagement(session -> session
                    .maximumSessions(1) // Limit to 1 session per user for security
                    .maxSessionsPreventsLogin(false)
                    .sessionRegistry(sessionRegistry())
                    .expiredUrl("/login")// Register the session registry
                )
                .csrf(csrf -> csrf.disable()); // Disabling CSRF for simplicity in this project

        return http.build();
    }
}
