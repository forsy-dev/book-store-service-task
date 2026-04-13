package com.forsy.controller;

import com.forsy.dto.AddToCartDto;
import com.forsy.dto.BookDto;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller responsible for handling all book-related web requests.
 *
 * <p>Provides endpoints for the complete CRUD lifecycle of books within the online
 * bookstore. Includes support for paginated catalog viewing, keyword searching,
 * and secures modification endpoints (create, update, delete) for employee access.
 *
 * @author Illia
 */
@Controller
@RequestMapping(WebConstants.URL_BOOKS)
@RequiredArgsConstructor
@Slf4j
public class BookController {

  private final BookService bookService;

  /**
   * Retrieves a paginated list of books, optionally filtered by a search keyword.
   *
   * @param model    the Spring MVC model to populate with book data
   * @param pageable the pagination and sorting information
   *                 (defaults to 10 per page, sorted by name)
   * @param keyword  an optional search string to filter books by name
   * @return the name of the view rendering the book catalog
   */
  @GetMapping
  public String getAllBooks(Model model,
                            @PageableDefault(sort = "name",
                                direction = Sort.Direction.ASC) Pageable pageable,
                            @RequestParam(name = WebConstants.PARAM_KEYWORD, required = false)
                              String keyword) {
    if (keyword != null && keyword.trim().isEmpty()) {
      keyword = null;
    }
    Page<BookDto> bookPage = bookService.getAllBooks(pageable, keyword);
    model.addAttribute(WebConstants.ATTR_BOOK_PAGE, bookPage);
    model.addAttribute(WebConstants.ATTR_KEYWORD, keyword);
    return WebConstants.VIEW_BOOKS;
  }

  /**
   * Retrieves detailed information for a specific book by its name.
   *
   * <p>Also initializes the "Add to Cart" DTO required by the view's forms.
   *
   * @param model          the Spring MVC model to populate with the book details
   * @param name           the exact name of the book to retrieve
   * @param authentication the current user's authentication details (used for logging)
   * @return the name of the view rendering the book details
   */
  @GetMapping(WebConstants.PATH_VAR_NAME)
  public String getBookByName(Model model,
                              @PathVariable(name = "name") String name,
                              Authentication authentication) {
    log.info("User {} fetching details for book: {}", authentication.getName(), name);

    BookDto book = bookService.getBookByName(name);
    model.addAttribute(WebConstants.ATTR_BOOK, book);

    if (!model.containsAttribute(WebConstants.ATTR_ADD_TO_CART_DTO)) {
      model.addAttribute(WebConstants.ATTR_ADD_TO_CART_DTO,
          AddToCartDto.builder().quantity(1).build());
    }

    return WebConstants.VIEW_BOOK_DETAIL;
  }

  /**
   * Serves the HTML form used to create a new book entry in the system.
   *
   * @param model          the Spring MVC model to populate with an empty book DTO
   * @param authentication the current employee's authentication details (used for logging)
   * @return the name of the view rendering the book creation form
   */
  @GetMapping(WebConstants.PATH_NEW)
  public String getNewBookForm(Model model, Authentication authentication) {
    log.info("User {} is requesting the 'new book' form", authentication.getName());

    model.addAttribute(WebConstants.ATTR_BOOK, new BookDto());
    return WebConstants.VIEW_BOOK_FORM;
  }

  /**
   * Processes the submission of a new book form.
   *
   * <p>Performs backend validation on the submitted DTO. If validation fails or
   * an exception occurs during saving, the user is returned to the form with error messages.
   *
   * @param bookDto        the data transfer object containing the new book details
   * @param bindingResult  holds the results of the validation and binding process
   * @param authentication the current employee's authentication details
   * @param model          the Spring MVC model to attach error messages if saving fails
   * @return a redirect to the book catalog on success, or the form view on failure
   */
  @PostMapping
  public String addBook(@Valid @ModelAttribute(WebConstants.ATTR_BOOK) BookDto bookDto,
                        BindingResult bindingResult,
                        Authentication authentication,
                        Model model) {
    log.info("Employee {} is attempting to add book: {}",
        authentication.getName(), bookDto.getName());

    if (bindingResult.hasErrors()) {
      log.warn("Validation errors while adding book: {}", bindingResult.getAllErrors());
      return WebConstants.VIEW_BOOK_FORM;
    }
    try {
      bookService.addBook(bookDto);
      log.info("Book {} added successfully by {}", bookDto.getName(), authentication.getName());
      return WebConstants.redirect(WebConstants.URL_BOOKS);
    } catch (Exception ex) {
      log.warn("Error adding book: {}", ex.getMessage());

      model.addAttribute(WebConstants.ATTR_ERROR_MESSAGE, ex.getMessage());
      return WebConstants.VIEW_BOOK_FORM;
    }
  }

  /**
   * Serves the HTML form used to edit an existing book.
   *
   * @param model          the Spring MVC model to populate with the existing book's data
   * @param name           the name of the book to edit
   * @param authentication the current employee's authentication details (used for logging)
   * @return the name of the view rendering the book edit form
   */
  @GetMapping(WebConstants.PATH_VAR_NAME + WebConstants.PATH_EDIT)
  public String getEditBookForm(Model model,
                                @PathVariable(name = "name") String name,
                                Authentication authentication) {
    log.info("Employee {} is requesting edit form for book: {}", authentication.getName(), name);

    BookDto book = bookService.getBookByName(name);
    model.addAttribute(WebConstants.ATTR_BOOK, book);
    model.addAttribute(WebConstants.ATTR_IS_EDIT, true);
    return WebConstants.VIEW_BOOK_FORM;
  }

  /**
   * Processes the submission of an updated book form.
   *
   * <p>Performs validation on the updated data before saving it to the database.
   *
   * @param name           the original name of the book being updated
   * @param bookDto        the data transfer object containing the updated book details
   * @param bindingResult  holds the results of the validation and binding process
   * @param authentication the current employee's authentication details
   * @return a redirect to the book catalog on success, or the form view on failure
   */
  @PutMapping(WebConstants.PATH_VAR_NAME)
  public String updateBookByName(@PathVariable(name = "name") String name,
                                 @Valid @ModelAttribute(WebConstants.ATTR_BOOK) BookDto bookDto,
                                 BindingResult bindingResult,
                                 Authentication authentication) {
    log.info("Employee {} is attempting to update book: {}", authentication.getName(), name);

    if (bindingResult.hasErrors()) {
      log.warn("Validation errors while updating book: {}", bindingResult.getAllErrors());
      return WebConstants.VIEW_BOOK_FORM;
    }
    bookService.updateBookByName(name, bookDto);
    log.info("Book {} updated successfully by {}", name, authentication.getName());
    return WebConstants.redirect(WebConstants.URL_BOOKS);
  }

  /**
   * Deletes a specific book from the system by its name.
   *
   * @param name           the exact name of the book to delete
   * @param authentication the current employee's authentication details
   * @return a redirect string to return the user to the updated book catalog
   */
  @DeleteMapping(WebConstants.PATH_VAR_NAME)
  public String deleteBookByName(@PathVariable(name = "name") String name,
                                 Authentication authentication) {
    log.info("Employee {} is attempting to delete book: {}", authentication.getName(), name);

    bookService.deleteBookByName(name);
    log.info("Book {} deleted successfully by {}", name, authentication.getName());
    return WebConstants.redirect(WebConstants.URL_BOOKS);
  }
}
