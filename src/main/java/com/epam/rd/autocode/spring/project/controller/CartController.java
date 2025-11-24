package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.AddToCartDTO;
import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.CartItemDisplayDTO;
import com.epam.rd.autocode.spring.project.service.BookService;
import com.epam.rd.autocode.spring.project.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    /**
     * Handles POST requests to add a book to the session-based cart.
     */
    @PostMapping("/add")
    public String addBookToCart(@Valid @ModelAttribute("addToCartDTO") AddToCartDTO dto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                HttpSession session,
                                HttpServletRequest request) {

        log.info("Trying to add {} of {} to cart", dto.getBookName(), dto.getQuantity());

        String referer = request.getHeader("Referer");
        String redirectUrl = "redirect:" + (referer != null ? referer : "/books");

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors while adding book to cart: {}", bindingResult.getAllErrors());
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.addToCartDTO", bindingResult);
            redirectAttributes.addFlashAttribute("addToCartDTO", dto);

            return redirectUrl;
        }
        // 1. Get cart from session, or create it
        Map<String, Integer> cart = (Map<String, Integer>) session.getAttribute("CART");
        if (cart == null) {
            cart = new HashMap<>();
        }

        // 2. Add item to cart
        cartService.addBookToCart(cart, dto);
        session.setAttribute("CART", cart);
        log.info("Added {} of {} to cart", dto.getBookName(), dto.getQuantity());

        // 3. Redirect back to the page the user came from
        return redirectUrl;
    }

    /**
     * Handles GET requests to display the cart page.
     */
//    @GetMapping
//    public String showCart(HttpSession session, Model model) {
//        Map<String, Integer> cart = (Map<String, Integer>) session.getAttribute("CART");
//        if (cart == null) {
//            cart = new HashMap<>();
//        }
//
//        List<CartItemDisplayDTO> cartItems = new ArrayList<>();
//        BigDecimal totalCost = BigDecimal.ZERO;
//
//        // 3. Convert book names from cart into full DTOs for display
//        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
//            String bookName = entry.getKey();
//            Integer quantity = entry.getValue();
//            try {
//                BookDTO book = bookService.getBookByName(bookName);
//                BigDecimal subtotal = book.getPrice().multiply(BigDecimal.valueOf(quantity));
//                cartItems.add(new CartItemDisplayDTO(book, quantity, subtotal));
//                totalCost = totalCost.add(subtotal);
//            } catch (Exception e) {
//                log.warn("Could not find book {} in cart. Skipping.", bookName);
//                // Optionally remove it from the cart
//                // cart.remove(bookName);
//            }
//        }
//
//        model.addAttribute("cartItems", cartItems);
//        model.addAttribute("totalCost", totalCost);
//        log.info("Displaying cart with {} items, total cost {}", cartItems.size(), totalCost);
//
//        return "cart";
//    }
}
