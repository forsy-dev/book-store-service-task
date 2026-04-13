package com.forsy.repo;

import com.forsy.model.Client;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing persistent {@link Client} entities.
 *
 * <p>This interface provides the data access abstraction for client-related
 * operations. It includes essential methods for unique identity verification,
 * credential retrieval, and advanced, paginated searching across client
 * profiles within the bookstore.
 *
 * @author Illia
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

  /**
   * Checks if a client already exists in the system with the specified email.
   *
   * <p>Primarily used during registration to enforce unique identity constraints.
   *
   * @param email the email address to check for existence
   * @return true if a client with the given email exists, false otherwise
   */
  boolean existsByEmail(String email);

  /**
   * Retrieves a client based on their unique email address.
   *
   * <p>This is the primary method for retrieving client profiles during
   * the authentication and authorization process.
   *
   * @param email the unique email address of the client
   * @return an {@link Optional} containing the found client, or empty if none matches
   */
  Optional<Client> findByEmail(String email);

  /**
   * Performs a case-insensitive search for clients by name or email, supporting pagination.
   *
   * <p>Allows administrators to locate clients using partial strings for either
   * their display name or their contact email, returning a paginated result set.
   *
   * @param name     the partial name string to match
   * @param email    the partial email string to match
   * @param pageable the pagination and sorting configuration
   * @return a {@link Page} of clients matching the search criteria
   */
  Page<Client> findAllByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
      String name, String email, Pageable pageable);
}
