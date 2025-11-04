package com.epam.rd.autocode.spring.project.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
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
                        .defaultSuccessUrl("/books", true)
                        .permitAll() // Allow everyone to see the login page
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login") // Redirect to home page on logout
                        .permitAll()
                )
                .csrf(csrf -> csrf.disable()); // Disabling CSRF for simplicity in this project

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        // Create an Employee user
        UserDetails employee = User.builder()
                .username("john.doe@email.com")
                .password(passwordEncoder.encode("pass123"))
                .roles("EMPLOYEE")
                .build();

        // Create a Client user
        UserDetails client = User.builder()
                .username("client1@example.com")
                .password(passwordEncoder.encode("password123"))
                .roles("CLIENT")
                .build();

        return new InMemoryUserDetailsManager(employee, client);
    }
}
