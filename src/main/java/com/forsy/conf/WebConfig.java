package com.forsy.conf;

import java.time.Duration;
import java.util.Locale;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

/**
 * Configuration class for Spring MVC web settings.
 *
 * <p>Specifically configures internationalization (i18n) and localization properties,
 * allowing the application to resolve and switch user languages based on URL parameters
 * and store those preferences securely in browser cookies.
 *
 * @author Illia
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

  /**
   * Configures the locale resolver to store language preferences in a cookie.
   *
   * <p>The cookie is named "LANG", persists for one year, and defaults to English
   * if no preference has been set by the user. It is available across the entire application.
   *
   * @return a configured {@link CookieLocaleResolver} instance
   */
  @Bean
  public LocaleResolver localeResolver() {
    CookieLocaleResolver clr = new CookieLocaleResolver("LANG");
    clr.setDefaultLocale(Locale.ENGLISH);
    clr.setCookieMaxAge(Duration.ofDays(365)); // Remember for 1 year
    clr.setCookiePath("/"); // Available for the whole app
    return clr;
  }

  /**
   * Configures an interceptor to detect language change requests.
   *
   * <p>This interceptor monitors all incoming HTTP requests for a specific URL parameter
   * (e.g., {@code ?lang=uk} or {@code ?lang=en}) and triggers a locale switch
   * when detected.
   *
   * @return a configured {@link LocaleChangeInterceptor} instance
   */
  @Bean
  public LocaleChangeInterceptor localeChangeInterceptor() {
    LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
    lci.setParamName("lang"); // Look for ?lang=xx in URL
    return lci;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(localeChangeInterceptor()).addPathPatterns("/**");
  }
}
