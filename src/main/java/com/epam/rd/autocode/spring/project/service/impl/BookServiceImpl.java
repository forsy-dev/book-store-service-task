package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final ModelMapper mapper;

    @Override
    public Page<BookDTO> getAllBooks(Pageable pageable, String keyword) {
        Page<Book> books;

        if (keyword != null && !keyword.trim().isEmpty()) {
            books = bookRepository.findAllByNameContainingIgnoreCaseOrAuthorContainingIgnoreCase(keyword, keyword, pageable);
        } else {
            books = bookRepository.findAll(pageable);
        }

        return books.map(book -> mapper.map(book, BookDTO.class));
    }

    @Override
    public BookDTO getBookByName(String name) {
        return bookRepository.findByName(name).map(book -> mapper.map(book, BookDTO.class))
                .orElseThrow(() -> new NotFoundException(String.format("Book with name %s not found", name)));
    }

    @Override
    public BookDTO updateBookByName(String name, BookDTO dto) {
        log.info("Attempting to update book with name {}", name);
        Book book = bookRepository.findByName(name).orElseThrow(
                () -> new NotFoundException(String.format("Book with name %s not found", name)));
        mapper.map(dto, book);
        book = bookRepository.save(book);
        log.info("Book with name {} updated successfully", name);
        return mapper.map(book, BookDTO.class);
    }

    @Override
    public void deleteBookByName(String name) {
        log.info("Attempting to delete book with name {}", name);
        bookRepository.findByName(name).ifPresentOrElse(book -> {
                    bookRepository.delete(book);
                    log.info("Book with name {} deleted successfully", name);
                },
                () -> {
                    throw new NotFoundException(String.format("Book with name %s not found", name));
                });
    }

    @Override
    public BookDTO addBook(BookDTO dto) {
        log.info("Attempting to add book with name {}", dto.getName());
        if (bookRepository.existsByName(dto.getName())) {
            throw new AlreadyExistException(String.format("Book with name %s already exists", dto.getName()));
        }
        Book book = bookRepository.save(mapper.map(dto, Book.class));
        log.info("Book with name {} added successfully", book.getName());
        return mapper.map(book, BookDTO.class);
    }
}
