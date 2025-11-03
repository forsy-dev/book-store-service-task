package com.epam.rd.autocode.spring.project.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig{

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        // --- Public Endpoints ---
                        // Allow anyone to see the home page, login/register
                        .requestMatchers("/", "/home", "/register").permitAll()
                        // Allow access to static resources
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()

                        // --- Employee Endpoints ---
                        // Only employees can add, edit, or delete books
                        .requestMatchers(HttpMethod.GET, "/books/new", "/books/*/edit").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.POST, "/books", "/books/**").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.PUT, "/books", "/books/**").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.DELETE, "/books", "/books/**").hasRole("EMPLOYEE") // Corrected typo
                        // Only employees can manage clients
                        .requestMatchers("/clients", "/clients/**").hasRole("EMPLOYEE")

                        // --- Authenticated Users ---
                        // Any other request must be authenticated.
                        // This now correctly includes GET /books, GET /books/{name},
                        // placing orders, and viewing profiles.
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login") // Specify custom login page
                        .permitAll() // Allow everyone to see the login page
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/") // Redirect to home page on logout
                        .permitAll()
                )
                .csrf(csrf -> csrf.disable()); // Disabling CSRF for simplicity in this project

        return http.build();
    }
}
