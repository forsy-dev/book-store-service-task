package com.forsy.service.impl;

import com.forsy.dto.BookDto;
import com.forsy.exception.AlreadyExistException;
import com.forsy.exception.NotFoundException;
import com.forsy.model.Book;
import com.forsy.repo.BookRepository;
import com.forsy.service.BookService;
import com.forsy.util.MessageKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Implementation of the {@link BookService} interface for managing the bookstore's inventory.
 *
 * <p>This service provides concrete business logic for book-related operations,
 * utilizing a {@link BookRepository} for data persistence and a {@link ModelMapper}
 * for seamless transformation between Entity and DTO layers. It leverages
 * {@link MessageSource} for internationalized error reporting and supports
 * comprehensive logging for administrative oversight.
 *
 * @author Illia
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {

  private final BookRepository bookRepository;
  private final ModelMapper mapper;
  private final MessageSource messageSource;

  /**
   * {@inheritDoc}
   *
   * <p>If a keyword is provided, searches across both book titles and
   * author names (case-insensitive). Otherwise, retrieves all books.
   */
  @Override
  public Page<BookDto> getAllBooks(Pageable pageable, String keyword) {
    Page<Book> books;

    if (keyword != null && !keyword.trim().isEmpty()) {
      books = bookRepository.findAllByNameContainingIgnoreCaseOrAuthorContainingIgnoreCase(
          keyword, keyword, pageable);
    } else {
      books = bookRepository.findAll(pageable);
    }

    return books.map(book -> mapper.map(book, BookDto.class));
  }

  /**
   * {@inheritDoc}
   *
   * @throws NotFoundException if no book is found with the specified title
   */
  @Override
  public BookDto getBookByName(String name) {
    return bookRepository.findByName(name)
        .map(book -> mapper.map(book, BookDto.class))
        .orElseThrow(() -> {
          String message = messageSource.getMessage(
              MessageKeys.ERROR_BOOK_NOT_FOUND, new Object[]{name},
              LocaleContextHolder.getLocale());
          return new NotFoundException(message);
        });
  }

  /**
   * {@inheritDoc}
   *
   * <p>Maps the provided DTO onto the existing entity, updates the
   * persistent state, and returns the modified result.
   *
   * @throws NotFoundException if the book to be updated does not exist
   */
  @Override
  public BookDto updateBookByName(String name, BookDto dto) {
    log.info("Attempting to update book with name {}", name);
    Book book = bookRepository.findByName(name)
        .orElseThrow(() -> {
          String message = messageSource.getMessage(
              MessageKeys.ERROR_BOOK_NOT_FOUND, new Object[]{name},
              LocaleContextHolder.getLocale());
          return new NotFoundException(message);
        });

    mapper.map(dto, book);
    book = bookRepository.save(book);
    log.info("Book with name {} updated successfully", name);
    return mapper.map(book, BookDto.class);
  }

  /**
   * {@inheritDoc}
   *
   * @throws NotFoundException if the book to be deleted does not exist
   */
  @Override
  public void deleteBookByName(String name) {
    log.info("Attempting to delete book with name {}", name);
    bookRepository.findByName(name).ifPresentOrElse(
        book -> {
          bookRepository.delete(book);
          log.info("Book with name {} deleted successfully", name);
        },
        () -> {
          String message = messageSource.getMessage(
              MessageKeys.ERROR_BOOK_NOT_FOUND, new Object[]{name},
              LocaleContextHolder.getLocale());
          throw new NotFoundException(message);
        });
  }

  /**
   * {@inheritDoc}
   *
   * <p>Verifies title uniqueness before persisting the new record.
   *
   * @throws AlreadyExistException if a book with the same name already exists
   */
  @Override
  public BookDto addBook(BookDto dto) {
    log.info("Attempting to add book with name {}", dto.getName());
    if (bookRepository.existsByName(dto.getName())) {
      String message = messageSource.getMessage(
          MessageKeys.ERROR_BOOK_ALREADY_EXISTS,
          new Object[]{dto.getName()}, LocaleContextHolder.getLocale());
      throw new AlreadyExistException(message);
    }

    Book book = bookRepository.save(mapper.map(dto, Book.class));
    log.info("Book with name {} added successfully", book.getName());
    return mapper.map(book, BookDto.class);
  }
}
