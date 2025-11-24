package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.AddToCartDTO;
import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
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
    public String getAllBooks(Model model,
                              @PageableDefault(size = 10, sort = "name") Pageable pageable,
                              Authentication authentication) {
        log.info("User {} fetching book list for page: {}",
                authentication.getName(), pageable.getPageNumber());

        Page<BookDTO> bookPage = bookService.getAllBooks(pageable);
        model.addAttribute("bookPage", bookPage);
        return "books";
    }

    @GetMapping("/{name}")
    public String getBookByName(Model model,
                                @PathVariable(name="name") String name,
                                Authentication authentication) {
        log.info("User {} fetching details for book: {}", authentication.getName(), name);

        BookDTO book = bookService.getBookByName(name);
        model.addAttribute("book", book);

        if (!model.containsAttribute("addToCartDTO")) {
            model.addAttribute("addToCartDTO", AddToCartDTO.builder().quantity(1).build());
        }

        return "book-detail";
    }

    @GetMapping("/new")
    public String getNewBookForm(Model model, Authentication authentication) {
        log.info("User {} is requesting the 'new book' form", authentication.getName());

        model.addAttribute("book", new BookDTO());
        return "book-form";
    }

    @PostMapping
    public String addBook(@Valid @ModelAttribute("book") BookDTO bookDTO,
                          BindingResult bindingResult,
                          Authentication authentication,
                          Model model) {
        log.info("Employee {} is attempting to add book: {}", authentication.getName(), bookDTO.getName());

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors while adding book: {}", bindingResult.getAllErrors());
            return "book-form";
        }
        try {
            bookService.addBook(bookDTO);
            log.info("Book {} added successfully by {}", bookDTO.getName(), authentication.getName());
            return "redirect:/books-list";
        } catch (Exception ex) {
            log.warn("Error adding book: {}", ex.getMessage());

            model.addAttribute("errorMessage", ex.getMessage());
            return "book-form";
        }
    }

    @GetMapping("/{name}/edit")
    public String getEditBookForm(Model model,
                                  @PathVariable(name="name") String name,
                                  Authentication authentication) {
        log.info("Employee {} is requesting edit form for book: {}", authentication.getName(), name);

        BookDTO book = bookService.getBookByName(name);
        model.addAttribute("book", book);
        model.addAttribute("isEdit", true);
        return "book-form";
    }

    @PutMapping("/{name}")
    public String updateBookByName(@PathVariable(name="name") String name,
                                   @Valid @ModelAttribute("book") BookDTO bookDTO,
                                   BindingResult bindingResult,
                                   Authentication authentication) {
        log.info("Employee {} is attempting to update book: {}", authentication.getName(), name);

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors while updating book: {}", bindingResult.getAllErrors());
            return "book-form";
        }
        bookService.updateBookByName(name, bookDTO);
        log.info("Book {} updated successfully by {}", name, authentication.getName());
        return "redirect:/books-list";
    }

    @DeleteMapping("/{name}")
    public String deleteBookByName(@PathVariable(name="name") String name,
                                   Authentication authentication) {
        log.info("Employee {} is attempting to delete book: {}", authentication.getName(), name);

        bookService.deleteBookByName(name);
        log.info("Book {} deleted successfully by {}", name, authentication.getName());
        return "redirect:/books";
    }
}
