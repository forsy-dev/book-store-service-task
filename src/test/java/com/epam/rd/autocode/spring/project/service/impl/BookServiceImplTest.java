package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

    @Nested
    class FindByName {

        @Test
        void testGetBookByName_ShouldReturnBook() {
            String name = "name";
            Book book = Book.builder().name(name).build();
            BookDTO expectedDto = BookDTO.builder().name(name).build();

            when(bookRepository.findByName(name)).thenReturn(Optional.of(book));
            when(mapper.map(book, BookDTO.class)).thenReturn(expectedDto);

            BookDTO actualBookDto = bookService.getBookByName(name);

            verify(bookRepository, times(1)).findByName(name);
            verify(mapper, times(1)).map(book, BookDTO.class);

            assertEquals(name, actualBookDto.getName());
        }

        @Test
        void testGetBookByName_ShouldThrowExceptionWhenBookNotFound() {
            String name = "name";

            when(bookRepository.findByName(name)).thenReturn(Optional.empty());

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
            BookDTO updateDto = BookDTO.builder().name(newName).build();
            BookDTO expectedDto = BookDTO.builder().name(newName).build();

            when(bookRepository.findByName(oldName)).thenReturn(Optional.of(existingBook));
            doNothing().when(mapper).map(updateDto, existingBook);
            when(bookRepository.save(existingBook)).thenReturn(existingBook);
            when(mapper.map(existingBook, BookDTO.class)).thenReturn(expectedDto);

            BookDTO actualBookDto = bookService.updateBookByName(oldName, updateDto);

            verify(bookRepository, times(1)).findByName(oldName);
            verify(mapper, times(1)).map(updateDto, existingBook);
            verify(bookRepository, times(1)).save(existingBook);
            verify(mapper, times(1)).map(existingBook, BookDTO.class);

            assertEquals(expectedDto, actualBookDto);
        }

        @Test
        void testUpdateBookByName_ShouldThrowExceptionWhenBookNotFound() {
            String oldName = "oldName";
            BookDTO updateDto = BookDTO.builder().build();

            when(bookRepository.findByName(oldName)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> bookService.updateBookByName(oldName, updateDto));

            verify(bookRepository, times(1)).findByName(oldName);
            verify(mapper, never()).map(any(BookDTO.class), any(Book.class));
            verify(bookRepository, never()).save(any(Book.class));
            verify(mapper, never()).map(any(Book.class), any());
        }
    }

    @Nested
    class DeleteByName {

        @Test
        void testDeleteBookByName_ShouldReturnNothing() {
            String name = "name";

            when(bookRepository.existsByName(name)).thenReturn(true);
            doNothing().when(bookRepository).deleteByName(name);

            bookService.deleteBookByName(name);

            verify(bookRepository, times(1)).existsByName(name);
            verify(bookRepository, times(1)).deleteByName(name);
        }
    }
}
