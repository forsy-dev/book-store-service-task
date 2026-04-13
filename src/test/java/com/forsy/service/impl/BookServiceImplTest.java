package com.forsy.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.forsy.dto.BookDto;
import com.forsy.exception.AlreadyExistException;
import com.forsy.exception.NotFoundException;
import com.forsy.model.Book;
import com.forsy.repo.BookRepository;
import com.forsy.util.MessageKeys;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class BookServiceImplTest {

  @InjectMocks
  private BookServiceImpl bookService;

  @Mock
  private BookRepository bookRepository;

  @Mock
  private ModelMapper mapper;

  @Mock
  private MessageSource messageSource;

  @Nested
  class GetAllBooks {

    @Test
    void testGetAllBooks_WhenKeywordNotGiven_ShouldReturnAllBooks() {
      Book book = Book.builder().build();
      BookDto expectedDto = new BookDto();
      Pageable pageable = PageRequest.of(0, 10);
      Page<Book> bookPage = new PageImpl<>(Collections.singletonList(book), pageable, 1);
      String keyword = "";

      when(bookRepository.findAll(pageable)).thenReturn(bookPage);
      when(mapper.map(book, BookDto.class)).thenReturn(expectedDto);

      Page<BookDto> actualBookDto = bookService.getAllBooks(pageable, keyword);

      verify(bookRepository, times(1)).findAll(pageable);
      verify(mapper, times(1)).map(book, BookDto.class);

      assertEquals(1, actualBookDto.getTotalElements());
      assertEquals(1, actualBookDto.getContent().size());
      assertEquals(expectedDto, actualBookDto.getContent().get(0));
    }

    @Test
    void testGetAllBooks_WhenKeywordGivenShouldReturnFoundBooks() {
      Book book = Book.builder().build();
      BookDto expectedDto = new BookDto();
      Pageable pageable = PageRequest.of(0, 10);
      Page<Book> bookPage = new PageImpl<>(Collections.singletonList(book), pageable, 1);
      String keyword = "test";

      when(bookRepository.findAllByNameContainingIgnoreCaseOrAuthorContainingIgnoreCase(keyword, keyword, pageable))
          .thenReturn(bookPage);
      when(mapper.map(book, BookDto.class)).thenReturn(expectedDto);

      Page<BookDto> actualBookDto = bookService.getAllBooks(pageable, keyword);

      verify(bookRepository, times(1))
          .findAllByNameContainingIgnoreCaseOrAuthorContainingIgnoreCase(keyword, keyword, pageable);
      verify(mapper, times(1)).map(book, BookDto.class);

      assertEquals(1, actualBookDto.getTotalElements());
      assertEquals(1, actualBookDto.getContent().size());
      assertEquals(expectedDto, actualBookDto.getContent().get(0));
    }
  }

  @Nested
  class FindByName {

    @Test
    void testGetBookByName_ShouldReturnBook() {
      String name = "name";
      Book book = Book.builder().name(name).build();
      BookDto expectedDto = BookDto.builder().name(name).build();

      when(bookRepository.findByName(name)).thenReturn(Optional.of(book));
      when(mapper.map(book, BookDto.class)).thenReturn(expectedDto);

      BookDto actualBookDto = bookService.getBookByName(name);

      verify(bookRepository, times(1)).findByName(name);
      verify(mapper, times(1)).map(book, BookDto.class);

      assertEquals(name, actualBookDto.getName());
    }

    @Test
    void testGetBookByName_ShouldThrowExceptionWhenBookNotFound() {
      String name = "name";
      String errorMessage = "Book not found";

      when(bookRepository.findByName(name)).thenReturn(Optional.empty());
      when(messageSource.getMessage(eq("error.book.not.found"), any(), any(Locale.class)))
          .thenReturn(errorMessage);

      assertThrows(NotFoundException.class, () -> bookService.getBookByName(name));

      verify(bookRepository, times(1)).findByName(name);
      verify(mapper, never()).map(any(Book.class), any());
    }
  }

  @Nested
  class UpdateByName {

    @Test
    void testUpdateBookByName_ShouldReturnBook() {
      String oldName = "oldName";
      String newName = "newName";
      Book existingBook = Book.builder().id(1L).name(oldName).build();
      BookDto updateDto = BookDto.builder().name(newName).build();
      BookDto expectedDto = BookDto.builder().name(newName).build();

      when(bookRepository.findByName(oldName)).thenReturn(Optional.of(existingBook));
      doNothing().when(mapper).map(updateDto, existingBook);
      when(bookRepository.save(existingBook)).thenReturn(existingBook);
      when(mapper.map(existingBook, BookDto.class)).thenReturn(expectedDto);

      BookDto actualBookDto = bookService.updateBookByName(oldName, updateDto);

      verify(bookRepository, times(1)).findByName(oldName);
      verify(mapper, times(1)).map(updateDto, existingBook);
      verify(bookRepository, times(1)).save(existingBook);
      verify(mapper, times(1)).map(existingBook, BookDto.class);

      assertEquals(expectedDto, actualBookDto);
    }

    @Test
    void testUpdateBookByName_ShouldThrowExceptionWhenBookNotFound() {
      String oldName = "oldName";
      BookDto updateDto = BookDto.builder().build();
      String errorMessage = "Book not found";

      when(bookRepository.findByName(oldName)).thenReturn(Optional.empty());
      when(messageSource.getMessage(eq("error.book.not.found"), any(), any(Locale.class))).thenReturn(errorMessage);

      assertThrows(NotFoundException.class, () -> bookService.updateBookByName(oldName, updateDto));

      verify(bookRepository, times(1)).findByName(oldName);
      verify(mapper, never()).map(any(BookDto.class), any(Book.class));
      verify(bookRepository, never()).save(any(Book.class));
      verify(mapper, never()).map(any(Book.class), any());
    }
  }

  @Nested
  class DeleteByName {

    @Test
    void testDeleteBookByName_ShouldReturnNothing() {
      String name = "name";
      Book book = Book.builder().name(name).build();

      when(bookRepository.findByName(name)).thenReturn(Optional.of(book));
      doNothing().when(bookRepository).delete(book);

      bookService.deleteBookByName(name);

      verify(bookRepository, times(1)).findByName(name);
      verify(bookRepository, times(1)).delete(book);
    }

    @Test
    void testDeleteBookByName_ShouldThrowExceptionWhenBookNotFound() {
      String name = "name";
      String errorMessage = "Book not found";

      when(bookRepository.findByName(name)).thenReturn(Optional.empty());
      when(messageSource.getMessage(eq("error.book.not.found"), any(), any(Locale.class))).thenReturn(errorMessage);

      assertThrows(NotFoundException.class, () -> bookService.deleteBookByName(name));

      verify(bookRepository, times(1)).findByName(name);
      verify(bookRepository, never()).delete(any(Book.class));
    }
  }

  @Nested
  class AddBook {

    @Test
    void testAddBook_ShouldReturnBook() {
      String name = "name";
      BookDto createDto = BookDto.builder().name(name).build();
      BookDto expectedDto = BookDto.builder().name(name).build();
      Book mappedBook = Book.builder().name(name).build();

      when(bookRepository.existsByName(name)).thenReturn(false);
      when(mapper.map(createDto, Book.class)).thenReturn(mappedBook);
      when(bookRepository.save(mappedBook)).thenReturn(mappedBook);
      when(mapper.map(mappedBook, BookDto.class)).thenReturn(expectedDto);

      BookDto actualBookDto = bookService.addBook(createDto);

      verify(bookRepository, times(1)).existsByName(name);
      verify(mapper, times(1)).map(createDto, Book.class);
      verify(bookRepository, times(1)).save(mappedBook);
      verify(mapper, times(1)).map(mappedBook, BookDto.class);

      assertEquals(expectedDto, actualBookDto);
    }

    @Test
    void testAddBook_ShouldThrowExceptionWhenBookAlreadyExists() {
      String name = "name";
      BookDto createDto = BookDto.builder().name(name).build();
      String errorMessage = "Book not found";

      when(bookRepository.existsByName(name)).thenReturn(true);
      when(messageSource.getMessage(eq(MessageKeys.ERROR_BOOK_ALREADY_EXISTS), any(), any(Locale.class))).thenReturn(errorMessage);

      assertThrows(AlreadyExistException.class, () -> bookService.addBook(createDto));

      verify(bookRepository, times(1)).existsByName(name);
      verify(mapper, never()).map(any(BookDto.class), any());
      verify(bookRepository, never()).save(any(Book.class));
      verify(mapper, never()).map(any(Book.class), any());
    }
  }
}
