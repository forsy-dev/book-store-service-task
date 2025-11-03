package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping
    public String getAllBooks(Model model, @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        Page<BookDTO> bookPage = bookService.getAllBooks(pageable);
        model.addAttribute("bookPage", bookPage);
        return "books-list";
    }

    @GetMapping("/{name}")
    public String getBookByName(Model model, @PathVariable(name="name") String name) {
        BookDTO book = bookService.getBookByName(name);
        model.addAttribute("book", book);
        return "book-detail";
    }

    @GetMapping("/new")
    public String getNewBookForm(Model model) {
        model.addAttribute("book", new BookDTO());
        return "book-form";
    }

    // TODO: Implement GET /books/new (Show 'add book' form - Employee)
    // TODO: Implement POST /books (Save new book - Employee)
    // TODO: Implement GET /books/{name}/edit (Show 'edit book' form - Employee)
    // TODO: Implement POST /books/{name}/update (Update book - Employee)
    // TODO: Implement POST /books/{name}/delete (Delete book - Employee)
}
