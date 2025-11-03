package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import com.epam.rd.autocode.spring.project.service.BookService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class BookSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Test
    void testGetBooks_WhenAnonymous_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/books"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Nested
    class GetBooks {

        @Test
        @WithMockUser(roles = "CLIENT")
        void testGetBooks_WhenAuthenticatedAsClient_ShouldAllowAccess() throws Exception {
            Page<BookDTO> bookPage = new PageImpl<>(Collections.singletonList(new BookDTO()));

            when(bookService.getAllBooks(any(Pageable.class))).thenReturn(bookPage);

            mockMvc.perform(get("/books")
                            .param("page", "0")
                            .param("size", "10")
                            .param("sort", "name"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        void testGetBooks_WhenAuthenticatedAsEmployee_ShouldAllowAccess() throws Exception {
            Page<BookDTO> bookPage = new PageImpl<>(Collections.singletonList(new BookDTO()));

            when(bookService.getAllBooks(any(Pageable.class))).thenReturn(bookPage);

            mockMvc.perform(get("/books")
                            .param("page", "0")
                            .param("size", "10")
                            .param("sort", "name"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    class GetBookByName {

        @Test
        @WithMockUser(roles = "CLIENT")
        void testGetBookByName_WhenAuthenticatedAsClient_ShouldAllowAccess() throws Exception {
            String bookName = "testbook";
            BookDTO bookDto = BookDTO.builder().name(bookName).build();

            when(bookService.getBookByName("testbook")).thenReturn(bookDto);

            mockMvc.perform(get("/books/{name}", bookName))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        void testGetBookByName_WhenAuthenticatedAsEmployee_ShouldAllowAccess() throws Exception {
            String bookName = "testbook";
            BookDTO bookDto = BookDTO.builder().name(bookName).build();

            when(bookService.getBookByName("testbook")).thenReturn(bookDto);

            mockMvc.perform(get("/books/{name}", bookName))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    class GetBookForm {

        @Test
        @WithMockUser(roles = "CLIENT")
        void testGetBookForm_WhenAuthenticatedAsClient_ShouldForbidAccess() throws Exception {
            mockMvc.perform(get("/books/new"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        void testGetBookForm_WhenAuthenticatedAsEmployee_ShouldAllowAccess() throws Exception {
            mockMvc.perform(get("/books/new"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    class AddBook {

        @Test
        @WithMockUser(roles = "CLIENT")
        void testAddBook_WhenAuthenticatedAsClient_ShouldForbidAccess() throws Exception {
            mockMvc.perform(post("/books"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        void testAddBook_WhenAuthenticatedAsEmployee_ShouldAllowAccess() throws Exception {
            BookDTO bookDto = BookDTO.builder()
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

            mockMvc.perform(post("/books")
                            .flashAttr("book", bookDto))
                    .andExpect(status().is3xxRedirection());
        }
    }

    @Nested
    class GetEditBookForm {

        @Test
        @WithMockUser(roles = "CLIENT")
        void testGetEditBookForm_WhenAuthenticatedAsClient_ShouldForbidAccess() throws Exception {
            String name = "testbook";
            mockMvc.perform(get("/books/{name}/edit", name))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        void testGetEditBookForm_WhenAuthenticatedAsEmployee_ShouldAllowAccess() throws Exception {
            BookDTO bookDto = BookDTO.builder()
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

            mockMvc.perform(get("/books/{name}/edit", bookDto.getName()))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    class EditBook {

        @Test
        @WithMockUser(roles = "CLIENT")
        void testEditBook_WhenAuthenticatedAsClient_ShouldForbidAccess() throws Exception {
            String name = "testbook";
            mockMvc.perform(put("/books/{name}", name))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        void testEditBook_WhenAuthenticatedAsEmployee_ShouldAllowAccess() throws Exception {
            BookDTO bookDto = BookDTO.builder()
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

            mockMvc.perform(put("/books/{name}", bookDto.getName())
                            .flashAttr("book", bookDto))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/books-list"));
        }
    }

    @Nested
    class DeleteBook {

        @Test
        @WithMockUser(roles = "CLIENT")
        void testDeleteBook_WhenAuthenticatedAsClient_ShouldForbidAccess() throws Exception {
            String name = "testbook";

            mockMvc.perform(delete("/books/{name}", name))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        void testDeleteBook_WhenAuthenticatedAsEmployee_ShouldAllowAccess() throws Exception {
            String name = "testbook";

            doNothing().when(bookService).deleteBookByName(name);

            mockMvc.perform(delete("/books/{name}", name))
                    .andExpect(status().is3xxRedirection());
        }
    }
}
