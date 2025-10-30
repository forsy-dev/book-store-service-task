package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.service.BookService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final ModelMapper mapper;

    @Override
    public List<BookDTO> getAllBooks() {
        return bookRepository.findAll().stream().map(book -> mapper.map(book, BookDTO.class)).toList();
    }

    @Override
    public BookDTO getBookByName(String name) {
        return bookRepository.findByName(name).map(book -> mapper.map(book, BookDTO.class))
                .orElseThrow(() -> new NotFoundException(String.format("Book with name %s not found", name)));
    }

    @Override
    public BookDTO updateBookByName(String name, BookDTO dto) {
        Book book = bookRepository.findByName(name).orElseThrow(
                () -> new NotFoundException(String.format("Book with name %s not found", name)));
        mapper.map(dto, book);
        return mapper.map(bookRepository.save(book), BookDTO.class);
    }

    @Override
    public void deleteBookByName(String name) {

    }

    @Override
    public BookDTO addBook(BookDTO book) {
        return null;
    }
    //TODO Place your code here
}
