package com.forsy.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.forsy.dto.BookDto;
import com.forsy.model.enums.AgeGroup;
import com.forsy.model.enums.Language;
import com.forsy.model.enums.Role;
import com.forsy.service.BookService;
import com.forsy.util.WebConstants;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class BookSecurityIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private BookService bookService;

  @Nested
  class GetBooks {

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"CLIENT", "EMPLOYEE"})
    void testGetBooksWhenAuthenticatedShouldAllowAccess(Role role) throws Exception {
      Page<BookDto> bookPage = new PageImpl<>(Collections.singletonList(new BookDto()));

      when(bookService.getAllBooks(any(Pageable.class), nullable(String.class)))
          .thenReturn(bookPage);

      mockMvc.perform(get(WebConstants.URL_BOOKS)
                          .with(SecurityMockMvcRequestPostProcessors.user("testUser")
                                    .roles(role.name())))
          .andExpect(status().isOk());
    }
  }

  @Nested
  class GetBookByName {

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"CLIENT", "EMPLOYEE"})
    void testGetBookByNameWhenAuthenticatedShouldAllowAccess(Role role) throws Exception {
      String bookName = "testbook";
      BookDto bookDto = BookDto.builder().name(bookName).build();

      when(bookService.getBookByName("testbook")).thenReturn(bookDto);

      mockMvc.perform(get(WebConstants.URL_BOOK_DETAIL, bookName)
                          .with(SecurityMockMvcRequestPostProcessors.user("testUser")
                                    .roles(role.name())))
          .andExpect(status().isOk());
    }
  }

  @Nested
  class GetBookForm {

    @Test
    @WithMockUser(roles = "CLIENT")
    void testGetBookFormWhenAuthenticatedAsClientShouldForbidAccess() throws Exception {
      mockMvc.perform(get(WebConstants.URL_BOOK_NEW))
          .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testGetBookFormWhenAuthenticatedAsEmployeeShouldAllowAccess() throws Exception {
      mockMvc.perform(get(WebConstants.URL_BOOK_NEW))
          .andExpect(status().isOk());
    }
  }

  @Nested
  class AddBook {

    @Test
    @WithMockUser(roles = "CLIENT")
    void testAddBookWhenAuthenticatedAsClientShouldForbidAccess() throws Exception {
      mockMvc.perform(post(WebConstants.URL_BOOKS).with(csrf()))
          .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testAddBookWhenAuthenticatedAsEmployeeShouldAllowAccess() throws Exception {
      BookDto bookDto = BookDto.builder()
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

      mockMvc.perform(post(WebConstants.URL_BOOKS)
                          .flashAttr(WebConstants.ATTR_BOOK, bookDto)
                          .with(csrf()))
          .andExpect(status().is3xxRedirection());
    }
  }

  @Nested
  class GetEditBookForm {

    @Test
    @WithMockUser(roles = "CLIENT")
    void testGetEditBookFormWhenAuthenticatedAsClientShouldForbidAccess() throws Exception {
      String name = "testbook";
      mockMvc.perform(get(WebConstants.URL_BOOK_DETAIL_EDIT, name))
          .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testGetEditBookFormWhenAuthenticatedAsEmployeeShouldAllowAccess() throws Exception {
      BookDto bookDto = BookDto.builder()
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

      when(bookService.getBookByName(bookDto.getName())).thenReturn(bookDto);

      mockMvc.perform(get(WebConstants.URL_BOOK_DETAIL_EDIT, bookDto.getName()))
          .andExpect(status().isOk());
    }
  }

  @Nested
  class EditBook {

    @Test
    @WithMockUser(roles = "CLIENT")
    void testEditBookWhenAuthenticatedAsClientShouldForbidAccess() throws Exception {
      String name = "testbook";
      mockMvc.perform(put(WebConstants.URL_BOOK_DETAIL, name).with(csrf()))
          .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testEditBookWhenAuthenticatedAsEmployeeShouldAllowAccess() throws Exception {
      BookDto bookDto = BookDto.builder()
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

      when(bookService.updateBookByName(bookDto.getName(), bookDto)).thenReturn(bookDto);

      mockMvc.perform(put(WebConstants.URL_BOOK_DETAIL, bookDto.getName())
                          .flashAttr(WebConstants.ATTR_BOOK, bookDto)
                          .with(csrf()))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl(WebConstants.URL_BOOKS));
    }
  }

  @Nested
  class DeleteBook {

    @Test
    @WithMockUser(roles = "CLIENT")
    void testDeleteBookWhenAuthenticatedAsClientShouldForbidAccess() throws Exception {
      String name = "testbook";

      mockMvc.perform(delete(WebConstants.URL_BOOK_DETAIL, name))
          .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void testDeleteBookWhenAuthenticatedAsEmployeeShouldAllowAccess() throws Exception {
      String name = "testbook";

      doNothing().when(bookService).deleteBookByName(name);

      mockMvc.perform(delete(WebConstants.URL_BOOK_DETAIL, name)
                          .with(csrf()))
          .andExpect(status().is3xxRedirection());
    }
  }
}
