package com.forsy.service.impl;

import com.forsy.model.Client;
import com.forsy.model.ClientBlockStatus;
import com.forsy.model.Employee;
import com.forsy.model.User;
import com.forsy.repo.ClientBlockStatusRepository;
import com.forsy.repo.ClientRepository;
import com.forsy.repo.EmployeeRepository;
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

    @Override
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

    private boolean isClientBlocked(String email) {
        Optional<ClientBlockStatus> statusOpt = blockStatusRepository.findByClientEmail(email);
        return statusOpt.map(ClientBlockStatus::isBlocked).orElse(false);
    }

    private UserDetails buildUserDetails(User user, String role, boolean isBlocked) {
        Collection<? extends GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + role));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                !isBlocked,
                true,
                true,
                true,
                authorities);
    }
}
