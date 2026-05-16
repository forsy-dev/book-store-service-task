package com.forsy.controller;

import com.forsy.dto.AddToCartDto;
import com.forsy.dto.CartItemDisplayDto;
import com.forsy.service.CartService;
import com.forsy.service.impl.CurrencyService;
import com.forsy.util.CartCookieUtil;
import com.forsy.util.WebConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller responsible for managing the user's shopping cart.
 *
 * <p>Handles web requests for adding items to the cart, viewing the cart's
 * contents, and removing items. To maintain a stateless backend architecture,
 * the cart's state is stored and retrieved entirely via client-side HTTP cookies.
 *
 * @author Illia
 */
@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

  private final CartService cartService;
  private final CartCookieUtil cartCookieUtil;
  private final CurrencyService currencyService;

  /**
   * Processes the addition of a book to the user's shopping cart.
   *
   * <p>Validates the incoming data transfer object. If validation fails, the user
   * is redirected back to the previous page with error attributes. On success,
   * the cart cookie is updated with the new item quantity.
   *
   * @param dto                the data transfer object containing the book name and quantity
   * @param bindingResult      holds the results of the DTO validation
   * @param redirectAttributes used to pass validation errors back to the referring page
   * @param request            the current HTTP request,
   *                           used to retrieve the existing cart cookie and referer URL
   * @param response           the HTTP response, used to save the updated cart cookie
   * @return a redirect URL string returning the user to their previous page
   */
  @PostMapping("/add")
  public String addBookToCart(@Valid @ModelAttribute("addToCartDTO") AddToCartDto dto,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              HttpServletRequest request,
                              HttpServletResponse response) {

    log.info("Trying to add {} of {} to cart", dto.getBookName(), dto.getQuantity());

    String referer = request.getHeader("Referer");
    String redirectUrl = "redirect:" + (referer != null ? referer : WebConstants.URL_BOOKS);

    if (bindingResult.hasErrors()) {
      log.warn("Validation errors while adding book to cart: {}", bindingResult.getAllErrors());
      redirectAttributes.addFlashAttribute(
          WebConstants.getBindingResultKey(WebConstants.ATTR_ADD_TO_CART_DTO), bindingResult);
      redirectAttributes.addFlashAttribute(WebConstants.ATTR_ADD_TO_CART_DTO, dto);

      return redirectUrl;
    }

    Map<String, Integer> cart = cartCookieUtil.getCartFromCookie(request);
    cartService.addBookToCart(cart, dto);
    cartCookieUtil.saveCartToCookie(response, cart);
    log.info("Added {} of {} to cart", dto.getBookName(), dto.getQuantity());

    return redirectUrl;
  }

  /**
   * Displays the contents of the user's shopping cart.
   *
   * <p>Retrieves the cart from the user's cookies, fetches the detailed information
   * for each book, and calculates the total cost in both USD and UAH using the
   * currency service before rendering the view.
   *
   * @param request the current HTTP request, used to retrieve the cart cookie
   * @param model   the Spring MVC model to populate with cart items and total prices
   * @return the name of the view rendering the shopping cart page
   */
  @GetMapping
  public String showCart(HttpServletRequest request, Model model) {
    Map<String, Integer> cart = cartCookieUtil.getCartFromCookie(request);

    List<CartItemDisplayDto> cartItems = cartService.getCartItems(cart);
    BigDecimal totalPriceUsd = cartService.calculateTotalCost(cartItems);

    BigDecimal totalPriceUah = currencyService.convertUsdToUah(totalPriceUsd);

    model.addAttribute(WebConstants.ATTR_CART_ITEMS, cartItems);
    model.addAttribute(WebConstants.ATTR_TOTAL_PRICE_USD, totalPriceUsd);
    model.addAttribute(WebConstants.ATTR_TOTAL_PRICE_UAH, totalPriceUah);

    return WebConstants.VIEW_CART;
  }

  /**
   * Removes a specific book entirely from the user's shopping cart.
   *
   * <p>Updates the cart state by removing the specified entry and overwrites
   * the user's cart cookie before redirecting back to the cart view.
   *
   * @param bookName the exact name of the book to remove
   * @param request  the current HTTP request, used to retrieve the existing cart cookie
   * @param response the HTTP response, used to save the updated cart cookie
   * @return a redirect URL string returning the user to the cart page
   */
  @PostMapping("/remove")
  public String removeBookFromCart(@RequestParam("bookName") String bookName,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {
    Map<String, Integer> cart = cartCookieUtil.getCartFromCookie(request);
    cartService.removeBookFromCart(cart, bookName);
    cartCookieUtil.saveCartToCookie(response, cart);
    return WebConstants.redirect(WebConstants.URL_CART);
  }
}
