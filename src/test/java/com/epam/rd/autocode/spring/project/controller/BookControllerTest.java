package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.service.BookService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
public class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Nested
    class GetBooks {

        @Test
        void testGetAllBooks_ShouldReturnBooksList() throws Exception {
            Page<BookDTO> bookPage = new PageImpl<>(Collections.singletonList(new BookDTO()));
            when(bookService.getAllBooks(any(Pageable.class))).thenReturn(bookPage);

            mockMvc.perform(get("/books")
                            .param("page", "0")
                            .param("size", "10")
                            .param("sort", "name")
                            .with(user("testuser").roles("CLIENT")))
                    .andExpect(status().isOk())
                    .andExpect(view().name("books-list"))
                    .andExpect(model().attributeExists("bookPage"))
                    .andExpect(model().attribute("bookPage", bookPage));
        }
    }

    @Nested
    class GetBookByName {

        @Test
        void testGetBookByName_ShouldReturnBook() throws Exception {
            String bookName = "testbook";
            BookDTO bookDto = BookDTO.builder().name(bookName).build();
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


}

