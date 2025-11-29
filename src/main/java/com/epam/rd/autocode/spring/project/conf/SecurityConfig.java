package com.epam.rd.autocode.spring.project.conf;

import com.epam.rd.autocode.spring.project.conf.jwt.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/register", "/login", "/logout", "/").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
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
                        .requestMatchers("/clients", "/clients/**", "/employees/**").hasRole("EMPLOYEE")
                        .anyRequest().authenticated()
                )
            .exceptionHandling(e -> e
                // Handle 401 Unauthenticated -> Redirect to Login
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
                // Handle 403 Forbidden -> Forward to /access-denied
                .accessDeniedHandler(accessDeniedHandler())
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            request.getRequestDispatcher("/access-denied").forward(request, response);
        };
    }
}
