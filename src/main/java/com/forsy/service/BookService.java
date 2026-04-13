package com.forsy.service;

import com.forsy.dto.BookDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface defining the business logic for managing books.
 *
 * <p>This contract specifies the operations available for inventory management,
 * serving as a buffer between the underlying data repositories and the web
 * controllers. It ensures that all book-related transactions adhere to the
 * bookstore's business rules and data transformation requirements.
 *
 * @author Illia
 */
public interface BookService {

  /**
   * Retrieves a paginated collection of books, optionally filtered by a keyword.
   *
   * <p>The search is typically performed across book titles and authors
   * to facilitate discovery within the library.
   *
   * @param pageable the pagination and sorting parameters
   * @param keyword  the search term used to filter results; can be null or empty
   * @return a {@link Page} of {@link BookDto} objects matching the criteria
   */
  Page<BookDto> getAllBooks(Pageable pageable, String keyword);

  /**
   * Locates a specific book using its unique title.
   *
   * @param name the exact name of the book to retrieve
   * @return the {@link BookDto} representing the requested book
   * @throws com.forsy.exception.NotFoundException if no book is found with the given name
   */
  BookDto getBookByName(String name);

  /**
   * Updates the details of an existing book.
   *
   * @param name    the current name of the book to be modified
   * @param bookDto the data transfer object containing the updated information
   * @return the updated {@link BookDto} as persisted in the system
   * @throws com.forsy.exception.NotFoundException if the book to update does not exist
   */
  BookDto updateBookByName(String name, BookDto bookDto);

  /**
   * Removes a book from the library's inventory.
   *
   * @param name the name of the book to be deleted
   * @throws com.forsy.exception.NotFoundException if the book does not exist
   */
  void deleteBookByName(String name);

  /**
   * Registers a new book into the bookstore's collection.
   *
   * @param bookDto the data transfer object representing the new book
   * @return the {@link BookDto} of the newly created book, including its generated ID
   * @throws com.forsy.exception.AlreadyExistException if a book with the same name already exists
   */
  BookDto addBook(BookDto bookDto);
}
