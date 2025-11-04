package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.ClientBlockStatus;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.repo.ClientBlockStatusRepository;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final ClientBlockStatusRepository blockStatusRepository;

    /**
     * Locates the user based on the username (email) and wraps it in a Spring Security UserDetails object.
     * Performs a check for both Client and Employee tables.
     * @param email The username (email) to locate the user.
     * @return A fully populated UserDetails object.
     * @throws UsernameNotFoundException if the user is not found in either table.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Try to find the user as an Employee
        Optional<Employee> employeeOpt = employeeRepository.findByEmail(email);
        if (employeeOpt.isPresent()) {
            return buildUserDetails(employeeOpt.get(), "EMPLOYEE", false);
        }

        // 2. If not found, try to find the user as a Client
        Optional<Client> clientOpt = clientRepository.findByEmail(email);
        if (clientOpt.isPresent()) {
            Client client = clientOpt.get();
            // Perform the critical block check
            boolean isBlocked = isClientBlocked(client.getEmail());
            return buildUserDetails(client, "CLIENT", isBlocked);
        }

        // 3. If no user found in either table
        throw new UsernameNotFoundException("User not found with email: " + email);
    }

    /**
     * Checks the ClientBlockStatusRepository for the client's current status.
     * Assumes unblocked if status record does not exist (and creates a default entry).
     */
    private boolean isClientBlocked(String email) {
        Optional<ClientBlockStatus> statusOpt = blockStatusRepository.findByClientEmail(email);
        // If a status is found, return its value; otherwise, default to false.
        return statusOpt.map(ClientBlockStatus::isBlocked).orElse(false);
    }

    /**
     * Helper to construct the final UserDetails object.
     */
    private UserDetails buildUserDetails(User user, String role, boolean isBlocked) {
        Collection<? extends GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + role));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                !isBlocked, // Enabled: true if NOT blocked, false if blocked
                true,
                true,
                true,
                authorities);
    }
}
