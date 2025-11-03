package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/books")
@RequiredArgsConstructor
@Slf4j
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

    @PostMapping
    public String addBook(@Valid @ModelAttribute("book") BookDTO bookDTO,
                          BindingResult bindingResult,
                          Model model) {

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors while adding book: {}", bindingResult.getAllErrors());
            return "book-form";
        }
        try {
            bookService.addBook(bookDTO);
            return "redirect:/books-list";
        } catch (Exception ex) {
            log.error("Error adding book: {}", ex.getMessage());
            model.addAttribute("errorMessage", ex.getMessage());
            return "book-form";
        }
    }

    @GetMapping("/{name}/edit")
    public String getEditBookForm(Model model, @PathVariable(name="name") String name) {
        BookDTO book = bookService.getBookByName(name);
        model.addAttribute("book", book);
        model.addAttribute("isEdit", true);
        return "book-form";
    }

    // TODO: Implement GET /books/{name}/edit (Show 'edit book' form - Employee)
    // TODO: Implement POST /books/{name}/update (Update book - Employee)
    // TODO: Implement POST /books/{name}/delete (Delete book - Employee)
}
