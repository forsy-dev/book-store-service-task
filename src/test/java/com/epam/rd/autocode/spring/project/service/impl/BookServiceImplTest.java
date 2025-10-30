package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceImplTest {

    @InjectMocks
    private BookServiceImpl bookService;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ModelMapper mapper;

    @Test
    void testGetAllBooks_ShouldReturnAllBooks() {
        Book book = Book.builder().build();
        BookDTO expectedDto = new BookDTO();
        List<Book> expectedBooks = Arrays.asList(book);

        when(bookRepository.findAll()).thenReturn(expectedBooks);
        when(mapper.map(book, BookDTO.class)).thenReturn(expectedDto);

        List<BookDTO> actualBookDto = bookService.getAllBooks();

        verify(bookRepository, times(1)).findAll();
        verify(mapper, times(1)).map(book, BookDTO.class);

        assertEquals(expectedBooks.size(), actualBookDto.size());
        assertSame(expectedDto, actualBookDto.get(0));
    }
}
