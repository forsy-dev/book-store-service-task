package com.forsy.controller;

import com.forsy.dto.AddToCartDTO;
import com.forsy.dto.BookDTO;
import com.forsy.service.BookService;
import com.forsy.util.WebConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(WebConstants.URL_BOOKS)
@RequiredArgsConstructor
@Slf4j
public class BookController {

    private final BookService bookService;

    @GetMapping
    public String getAllBooks(Model model,
                              @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable,
                              @RequestParam(name = WebConstants.PARAM_KEYWORD, required = false) String keyword) {
        if (keyword != null && keyword.trim().isEmpty()) {
            keyword = null;
        }
        Page<BookDTO> bookPage = bookService.getAllBooks(pageable, keyword);
        model.addAttribute(WebConstants.ATTR_BOOK_PAGE, bookPage);
        model.addAttribute(WebConstants.ATTR_KEYWORD, keyword);
        return WebConstants.VIEW_BOOKS;
    }

    @GetMapping(WebConstants.PATH_VAR_NAME)
    public String getBookByName(Model model,
                                @PathVariable(name="name") String name,
                                Authentication authentication) {
        log.info("User {} fetching details for book: {}", authentication.getName(), name);

        BookDTO book = bookService.getBookByName(name);
        model.addAttribute(WebConstants.ATTR_BOOK, book);

        if (!model.containsAttribute(WebConstants.ATTR_ADD_TO_CART_DTO)) {
            model.addAttribute(WebConstants.ATTR_ADD_TO_CART_DTO, AddToCartDTO.builder().quantity(1).build());
        }

        return WebConstants.VIEW_BOOK_DETAIL;
    }

    @GetMapping(WebConstants.PATH_NEW)
    public String getNewBookForm(Model model, Authentication authentication) {
        log.info("User {} is requesting the 'new book' form", authentication.getName());

        model.addAttribute(WebConstants.ATTR_BOOK, new BookDTO());
        return WebConstants.VIEW_BOOK_FORM;
    }

    @PostMapping
    public String addBook(@Valid @ModelAttribute(WebConstants.ATTR_BOOK) BookDTO bookDTO,
                          BindingResult bindingResult,
                          Authentication authentication,
                          Model model) {
        log.info("Employee {} is attempting to add book: {}", authentication.getName(), bookDTO.getName());

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors while adding book: {}", bindingResult.getAllErrors());
            return WebConstants.VIEW_BOOK_FORM;
        }
        try {
            bookService.addBook(bookDTO);
            log.info("Book {} added successfully by {}", bookDTO.getName(), authentication.getName());
            return WebConstants.redirect(WebConstants.URL_BOOKS);
        } catch (Exception ex) {
            log.warn("Error adding book: {}", ex.getMessage());

            model.addAttribute(WebConstants.ATTR_ERROR_MESSAGE, ex.getMessage());
            return WebConstants.VIEW_BOOK_FORM;
        }
    }

    @GetMapping(WebConstants.PATH_VAR_NAME + WebConstants.PATH_EDIT)
    public String getEditBookForm(Model model,
                                  @PathVariable(name="name") String name,
                                  Authentication authentication) {
        log.info("Employee {} is requesting edit form for book: {}", authentication.getName(), name);

        BookDTO book = bookService.getBookByName(name);
        model.addAttribute(WebConstants.ATTR_BOOK, book);
        model.addAttribute(WebConstants.ATTR_IS_EDIT, true);
        return WebConstants.VIEW_BOOK_FORM;
    }

    @PutMapping(WebConstants.PATH_VAR_NAME)
    public String updateBookByName(@PathVariable(name="name") String name,
                                   @Valid @ModelAttribute(WebConstants.ATTR_BOOK) BookDTO bookDTO,
                                   BindingResult bindingResult,
                                   Authentication authentication) {
        log.info("Employee {} is attempting to update book: {}", authentication.getName(), name);

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors while updating book: {}", bindingResult.getAllErrors());
            return WebConstants.VIEW_BOOK_FORM;
        }
        bookService.updateBookByName(name, bookDTO);
        log.info("Book {} updated successfully by {}", name, authentication.getName());
        return WebConstants.redirect(WebConstants.URL_BOOKS);
    }

    @DeleteMapping(WebConstants.PATH_VAR_NAME)
    public String deleteBookByName(@PathVariable(name="name") String name,
                                   Authentication authentication) {
        log.info("Employee {} is attempting to delete book: {}", authentication.getName(), name);

        bookService.deleteBookByName(name);
        log.info("Book {} deleted successfully by {}", name, authentication.getName());
        return WebConstants.redirect(WebConstants.URL_BOOKS);
    }
}
