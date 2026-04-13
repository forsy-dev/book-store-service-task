package com.forsy.repo;

import com.forsy.model.Book;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing persistent {@link Book} entities.
 *
 * <p>This interface provides the data access abstraction for the bookstore's
 * inventory. Beyond standard CRUD operations, it includes specialized
 * querying capabilities for title uniqueness verification and advanced,
 * paginated searching across multiple metadata fields.
 *
 * @author Illia
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

  /**
   * Retrieves a book based on its exact, unique title.
   *
   * @param name the exact name of the book to search for
   * @return an {@link Optional} containing the found book, or empty if none matches
   */
  Optional<Book> findByName(String name);

  /**
   * Checks for the existence of a book with the specified name.
   *
   * <p>Utilized primarily during the creation or update process to ensure
   * title uniqueness within the library.
   *
   * @param name the name to check for existence
   * @return true if a book with the given name exists, false otherwise
   */
  boolean existsByName(String name);

  /**
   * Performs a case-insensitive search for books by name or author, supporting pagination.
   *
   * <p>This method allows clients to search the inventory using partial strings
   * for either the book title or the author's name, returning a slice of results
   * as defined by the provided pageable configuration.
   *
   * @param name     the partial name string to match against book titles
   * @param author   the partial name string to match against authors
   * @param pageable the pagination and sorting information
   * @return a {@link Page} of books matching the search criteria
   */
  Page<Book> findAllByNameContainingIgnoreCaseOrAuthorContainingIgnoreCase(
      String name, String author, Pageable pageable);
}
