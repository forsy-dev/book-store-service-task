package com.forsy.conf;

import com.forsy.conf.jwt.JwtAuthenticationFilter;
import com.forsy.model.enums.Role;
import com.forsy.util.WebConstants;
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
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Main security configuration for the application.
 *
 * <p>Enables web security and method-level security. Configures the HTTP
 * security filter chain to use stateless sessions, enforces role-based
 * access control (RBAC) across all endpoints, and integrates the custom
 * JWT authentication filter into the Spring Security pipeline.
 *
 * @author Illia
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  /**
   * Provides the password encoder bean used for hashing and verifying passwords.
   *
   * <p>Utilizes the BCrypt hashing algorithm, which is the standard recommendation
   * for secure password storage in Spring Security.
   *
   * @return a {@link PasswordEncoder} instance using BCrypt
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * Exposes the Spring Security {@link AuthenticationManager} as a bean.
   *
   * <p>This allows the authentication manager to be injected into custom
   * authentication services (such as a login controller) to programmatically
   * authenticate users.
   *
   * @param config the Spring Security authentication configuration
   * @return the configured {@link AuthenticationManager}
   * @throws Exception if an error occurs while retrieving the authentication manager
   */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  /**
   * Configures the main security filter chain for HTTP requests.
   *
   * <p>This configuration disables CSRF protection (as JWTs are used), sets session
   * management to stateless, explicitly defines role-based access rules for all
   * API endpoints, configures custom exception handlers for unauthenticated and
   * unauthorized requests, and registers the custom JWT filter.
   *
   * @param http the {@link HttpSecurity} object to configure
   * @return the fully built {@link SecurityFilterChain}
   * @throws Exception if an error occurs during HTTP security configuration
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .logout(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(authz -> authz
            .requestMatchers("/register", "/login", "/logout", "/").permitAll()
            .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
            .requestMatchers("/h2-console/**").permitAll()
            .requestMatchers(
                HttpMethod.PUT, "/profile").hasAnyRole(Role.EMPLOYEE.name(), Role.CLIENT.name())
            .requestMatchers(HttpMethod.PUT, "/clients/profile").hasRole(Role.CLIENT.name())
            .requestMatchers(HttpMethod.DELETE, "/clients/profile").hasRole(Role.CLIENT.name())
            .requestMatchers(
                HttpMethod.GET, "/books/new", "/books/*/edit").hasRole(Role.EMPLOYEE.name())
            .requestMatchers(
                HttpMethod.POST, WebConstants.URL_BOOKS, WebConstants.URL_BOOKS + "/**")
            .hasRole(Role.EMPLOYEE.name())
            .requestMatchers(
                HttpMethod.PUT, WebConstants.URL_BOOKS, WebConstants.URL_BOOKS + "/**")
            .hasRole(Role.EMPLOYEE.name())
            .requestMatchers(
                HttpMethod.DELETE, WebConstants.URL_BOOKS, WebConstants.URL_BOOKS + "/**")
            .hasRole(Role.EMPLOYEE.name())
            .requestMatchers("/cart/**").hasRole(Role.CLIENT.name())
            .requestMatchers("/orders/submit").hasRole(Role.CLIENT.name())
            .requestMatchers("/orders/*/cancel", "/orders/*/confirm").hasRole(Role.EMPLOYEE.name())
            .requestMatchers(
                "/clients", "/clients/**", "/employees/**").hasRole(Role.EMPLOYEE.name())
            .anyRequest().authenticated()
        )
        .exceptionHandling(e -> e
            // Handle 401 Unauthenticated -> Redirect to Login
            .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
            // Handle 403 Forbidden -> Forward to /access-denied
            .accessDeniedHandler(accessDeniedHandler())
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));
    http.csrf(csrf -> csrf.ignoringRequestMatchers(
        AntPathRequestMatcher.antMatcher("/h2-console/**")));
    return http.build();
  }

  /**
   * Provides a custom handler for access denied (403 Forbidden) exceptions.
   *
   * <p>Intercepts unauthorized requests and forwards the user to a dedicated
   * access-denied endpoint rather than displaying the default Spring Security error page.
   *
   * @return the customized {@link AccessDeniedHandler}
   */
  @Bean
  public AccessDeniedHandler accessDeniedHandler() {
    return (request, response, accessDeniedException) -> {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      request.getRequestDispatcher("/access-denied").forward(request, response);
    };
  }
}
