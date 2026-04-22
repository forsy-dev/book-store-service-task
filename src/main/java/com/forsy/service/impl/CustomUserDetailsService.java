package com.forsy.service.impl;

import com.forsy.model.Client;
import com.forsy.model.ClientBlockStatus;
import com.forsy.model.Employee;
import com.forsy.model.User;
import com.forsy.repo.ClientBlockStatusRepository;
import com.forsy.repo.ClientRepository;
import com.forsy.repo.EmployeeRepository;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Custom implementation of {@link UserDetailsService} to support multi-domain
 * authentication for both clients and employees.
 *
 * <p>This service acts as the bridge between Spring Security and the
 * bookstore's persistent data, retrieving user credentials and authorities
 * while accounting for administrative blocking status to manage
 * account enabled/disabled states.
 *
 * @author Illia
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final ClientRepository clientRepository;
  private final EmployeeRepository employeeRepository;
  private final ClientBlockStatusRepository blockStatusRepository;

  /**
   * Locates the user based on the email address across employee and
   * client domains.
   *
   * <p>Employees are prioritized during lookup. If a client is found,
   * their administrative block status is checked to determine if the
   * account should be marked as "enabled."
   *
   * @param email the email address identifying the user whose data is required
   * @return a fully populated user record (never {@code null})
   * @throws UsernameNotFoundException if the user could not be found or
   *                                   has no authorities
   */
  @Override
  @Cacheable(value = "userDetails", key = "#email")
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    Optional<Employee> employeeOpt = employeeRepository.findByEmail(email);
    if (employeeOpt.isPresent()) {
      return buildUserDetails(employeeOpt.get(), "EMPLOYEE", false);
    }

    Optional<Client> clientOpt = clientRepository.findByEmail(email);
    if (clientOpt.isPresent()) {
      Client client = clientOpt.get();
      boolean isBlocked = isClientBlocked(client.getEmail());
      return buildUserDetails(client, "CLIENT", isBlocked);
    }

    throw new UsernameNotFoundException("User not found with email: " + email);
  }

  /**
   * Checks the administrative status of a client.
   *
   * @param email the email of the client to check
   * @return {@code true} if the client is explicitly blocked,
   *     {@code false} otherwise
   */
  private boolean isClientBlocked(String email) {
    Optional<ClientBlockStatus> statusOpt = blockStatusRepository.findByClientEmail(email);
    return statusOpt.map(ClientBlockStatus::isBlocked).orElse(false);
  }

  /**
   * Constructs a Spring Security {@link UserDetails} object from an internal
   * user entity.
   *
   * @param user      the internal {@link User} entity (Employee or Client)
   * @param role      the raw role string (e.g., "CLIENT" or "EMPLOYEE")
   * @param isBlocked the block status used to set the account's enabled flag
   * @return a configured {@link UserDetails} instance for the security context
   */
  private UserDetails buildUserDetails(User user, String role, boolean isBlocked) {
    Collection<? extends GrantedAuthority> authorities = Collections.singletonList(
        new SimpleGrantedAuthority("ROLE_" + role));

    return new org.springframework.security.core.userdetails.User(
        user.getEmail(),
        user.getPassword(),
        !isBlocked, // isEnabled: false if the user is blocked
        true,       // accountNonExpired
        true,       // credentialsNonExpired
        true,       // accountNonLocked
        authorities);
  }
}
