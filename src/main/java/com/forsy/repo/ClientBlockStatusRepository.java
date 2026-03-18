package com.forsy.repo;

import com.forsy.model.ClientBlockStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientBlockStatusRepository extends JpaRepository<ClientBlockStatus, Long> {
    Optional<ClientBlockStatus> findByClientEmail(String email);
    boolean existsByClientEmail(String email);
}
