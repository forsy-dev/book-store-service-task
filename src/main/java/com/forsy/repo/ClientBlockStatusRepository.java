package com.forsy.repo;

import com.forsy.model.ClientBlockStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing persistent {@link ClientBlockStatus} entities.
 *
 * <p>This interface serves as the specialized data access layer for security
 * checks, allowing the system to verify and manage the administrative
 * standing of clients based on their unique email addresses.
 *
 * @author Illia
 */
@Repository
public interface ClientBlockStatusRepository extends JpaRepository<ClientBlockStatus, Long> {

  /**
   * Retrieves the blocking status associated with a specific client's email.
   *
   * @param email the unique email address of the client
   * @return an {@link Optional} containing the block status, or empty if no record exists
   */
  Optional<ClientBlockStatus> findByClientEmail(String email);

  /**
   * Checks if a blocking status record exists for the given client email.
   *
   * <p>Used during the security handshake to determine if a record must be
   * consulted before allowing further transactions.
   *
   * @param email the unique email address to check
   * @return true if a record for this email exists, false otherwise
   */
  boolean existsByClientEmail(String email);
}
