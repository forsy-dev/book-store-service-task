package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.service.BookService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
public class BookSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Nested
    class GetBooks {

        @Test
        void testGetBooks_WhenAnonymous_ShouldRedirectToLogin() throws Exception {
            mockMvc.perform(get("/books"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("**/login"));
        }

        @Test
        @WithMockUser(roles = "CLIENT")
        void testGetBooks_WhenAuthenticatedAsClient_ShouldAllowAccess() throws Exception {
            mockMvc.perform(get("/books"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        void testGetBooks_WhenAuthenticatedAsEmployee_ShouldAllowAccess() throws Exception {
            mockMvc.perform(get("/books"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    class GetBookByName {

        @Test
        void testGetBookByName_WhenAnonymous_ShouldRedirectToLogin() throws Exception {
            mockMvc.perform(get("/books/testbook"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("**/login"));
        }

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
        void testGetBookForm_WhenAnonymous_ShouldRedirectToLogin() throws Exception {
            mockMvc.perform(get("/books/new"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("**/login"));
        }

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
}
