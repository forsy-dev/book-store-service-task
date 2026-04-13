package com.forsy.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.forsy.conf.jwt.JwtUtils;
import com.forsy.dto.BookDto;
import com.forsy.exception.AlreadyExistException;
import com.forsy.exception.NotFoundException;
import com.forsy.model.enums.AgeGroup;
import com.forsy.model.enums.Language;
import com.forsy.service.BookService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BookController.class)
public class BookControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private BookService bookService;

  @MockBean
  private JwtUtils jwtUtils;

  @MockBean
  private UserDetailsService userDetailsService;

  @Nested
  class GetBooks {

    @Test
    void testGetAllBooks_ShouldReturnBooksList() throws Exception {
      Page<BookDto> bookPage = new PageImpl<>(Collections.singletonList(new BookDto()));
      when(bookService.getAllBooks(any(Pageable.class), nullable(String.class))).thenReturn(bookPage);

      mockMvc.perform(get("/books")
                          .with(user("testuser").roles("CLIENT")))
          .andExpect(status().isOk())
          .andExpect(view().name("books"))
          .andExpect(model().attributeExists("bookPage"))
          .andExpect(model().attribute("bookPage", bookPage));
    }
  }

  @Nested
  class GetBookByName {

    @Test
    void testGetBookByName_ShouldReturnBook() throws Exception {
      String bookName = "testbook";
      BookDto bookDto = BookDto.builder().name(bookName).build();
      when(bookService.getBookByName(bookName)).thenReturn(bookDto);

      mockMvc.perform(get("/books/{name}", bookName)
                          .with(user("testuser").roles("CLIENT")))
          .andExpect(status().isOk())
          .andExpect(view().name("book-detail"))
          .andExpect(model().attributeExists("book"))
          .andExpect(model().attribute("book", bookDto));
    }

    @Test
    void testGetBookByName_ShouldReturnError() throws Exception {
      String bookName = "testbook";
      when(bookService.getBookByName(bookName)).thenThrow(new NotFoundException("Book not found"));

      mockMvc.perform(get("/books/{name}", bookName)
                          .with(user("testuser").roles("CLIENT")))
          .andExpect(status().isNotFound())
          .andExpect(view().name("error"));
    }
  }

  @Nested
  class GetBookForm {

    @Test
    void testGetBookForm_ShouldReturnForm() throws Exception {
      BookDto bookDto = BookDto.builder().build();

      mockMvc.perform(get("/books/new")
                          .with(user("testuser").roles("EMPLOYEE")))
          .andExpect(status().isOk())
          .andExpect(view().name("book-form"))
          .andExpect(model().attributeExists("book"))
          .andExpect(model().attribute("book", bookDto));
    }
  }

  @Nested
  class AddBook {

    BookDto bookDto;

    @BeforeEach
    void setUp() {
      bookDto = BookDto.builder()
          .name("book")
          .genre("genre")
          .ageGroup(AgeGroup.ADULT)
          .price(BigDecimal.TEN)
          .publicationDate(LocalDate.now().minusYears(1))
          .author("author")
          .pages(100)
          .characteristics("characteristics")
          .description("description")
          .language(Language.ENGLISH)
          .build();
    }

    @Test
    void testAddBook_ShouldRedirectToBooks_WhenSuccess() throws Exception {
      when(bookService.addBook(any(BookDto.class))).thenReturn(bookDto);

      mockMvc.perform(post("/books")
                          .with(user("testuser").roles("EMPLOYEE"))
                          .with(csrf())
                          .flashAttr("book", bookDto))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/books"));
    }

    @Test
    void testAddBook_ShouldReturnToForm_WhenValidationFails() throws Exception {
      bookDto.setName("");

      when(bookService.addBook(any(BookDto.class))).thenReturn(bookDto);

      mockMvc.perform(post("/books")
                          .with(user("testuser").roles("EMPLOYEE"))
                          .with(csrf())
                          .flashAttr("book", bookDto))
          .andExpect(status().isOk())
          .andExpect(view().name("book-form"));
    }

    @Test
    void testAddBook_ShouldReturnToForm_WhenServiceExceptionThrown() throws Exception {
      when(bookService.addBook(any(BookDto.class)))
          .thenThrow(new AlreadyExistException("Book already exists"));

      mockMvc.perform(post("/books")
                          .with(user("testuser").roles("EMPLOYEE"))
                          .with(csrf())
                          .flashAttr("book", bookDto))
          .andExpect(status().isOk())
          .andExpect(view().name("book-form"));
    }
  }

  @Nested
  class GetEditBookForm {

    BookDto bookDto;

    @BeforeEach
    void setUp() {
      bookDto = BookDto.builder()
          .name("book")
          .genre("genre")
          .ageGroup(AgeGroup.ADULT)
          .price(BigDecimal.TEN)
          .publicationDate(LocalDate.now().minusYears(1))
          .author("author")
          .pages(100)
          .characteristics("characteristics")
          .description("description")
          .language(Language.ENGLISH)
          .build();
    }

    @Test
    void testGetEditBookForm_ShouldReturnToForm_WhenSuccess() throws Exception {

      when(bookService.getBookByName("book")).thenReturn(bookDto);

      mockMvc.perform(get("/books/{name}/edit", "book")
                          .with(user("testuser").roles("EMPLOYEE")))
          .andExpect(status().isOk())
          .andExpect(view().name("book-form"))
          .andExpect(model().attribute("book", bookDto))
          .andExpect(model().attribute("isEdit", true));
    }
  }

  @Nested
  class EditBook {

    BookDto bookDto;

    @BeforeEach
    void setUp() {
      bookDto = BookDto.builder()
          .name("book")
          .genre("genre")
          .ageGroup(AgeGroup.ADULT)
          .price(BigDecimal.TEN)
          .publicationDate(LocalDate.now().minusYears(1))
          .author("author")
          .pages(100)
          .characteristics("characteristics")
          .description("description")
          .language(Language.ENGLISH)
          .build();
    }

    @Test
    void testEditBook_ShouldRedirect_WhenSuccess() throws Exception {

      when(bookService.updateBookByName(bookDto.getName(), bookDto)).thenReturn(bookDto);

      mockMvc.perform(put("/books/{name}", bookDto.getName())
                          .with(user("testuser").roles("EMPLOYEE"))
                          .with(csrf())
                          .flashAttr("book", bookDto))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/books"));
    }

    @Test
    void testEditBook_ShouldReturnToForm_WhenValidationFails() throws Exception {
      bookDto.setName("a");

      mockMvc.perform(put("/books/{name}", bookDto.getName())
                          .with(user("testuser").roles("EMPLOYEE"))
                          .with(csrf())
                          .flashAttr("book", bookDto))
          .andExpect(status().isOk())
          .andExpect(view().name("book-form"));
    }

    @Test
    void testEditBook_ShouldReturnErrorPage_WhenBookNotFound() throws Exception {

      when(bookService.updateBookByName(bookDto.getName(), bookDto)).thenThrow(new NotFoundException("Book not found"));

      mockMvc.perform(put("/books/{name}", bookDto.getName())
                          .with(user("testuser").roles("EMPLOYEE"))
                          .with(csrf())
                          .flashAttr("book", bookDto))
          .andExpect(status().isNotFound())
          .andExpect(view().name("error"));
    }
  }

  @Nested
  class DeleteBook {

    @Test
    void testDeleteBook_ShouldRedirect_WhenSuccess() throws Exception {

      String bookName = "book";

      doNothing().when(bookService).deleteBookByName(bookName);

      mockMvc.perform(delete("/books/{name}", bookName)
                          .with(user("testuser").roles("EMPLOYEE"))
                          .with(csrf()))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/books"));
    }

    @Test
    void testDeleteBook_ShouldReturnErrorPage_WhenBookNotFound() throws Exception {

      String bookName = "book";

      doThrow(new NotFoundException("Book not found")).when(bookService).deleteBookByName(bookName);

      mockMvc.perform(delete("/books/{name}", bookName)
                          .with(user("testuser").roles("EMPLOYEE"))
                          .with(csrf()))
          .andExpect(status().isNotFound())
          .andExpect(view().name("error"));
    }
  }
}

