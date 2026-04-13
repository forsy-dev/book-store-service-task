package com.forsy.repo;

import com.forsy.model.Employee;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing persistent {@link Employee} entities.
 *
 * <p>This interface provides the data access abstraction for all staff-related
 * operations. It facilitates the unique identification of employees and
 * serves as a critical component in the bookstore's security and
 * administrative workflows.
 *
 * @author Illia
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

  /**
   * Checks if an employee already exists in the system with the specified email.
   *
   * <p>Utilized during the onboarding of new staff to prevent identity
   * duplication within the professional registry.
   *
   * @param email the unique professional email address to check
   * @return true if an employee with the given email exists, false otherwise
   */
  boolean existsByEmail(String email);

  /**
   * Retrieves an employee based on their unique professional email address.
   *
   * <p>This method is essential for the authentication layer, allowing
   * the system to verify staff credentials and load their specific
   * authorities.
   *
   * @param email the unique email address of the employee
   * @return an {@link Optional} containing the found employee, or empty if none matches
   */
  Optional<Employee> findByEmail(String email);
}
