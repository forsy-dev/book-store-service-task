package com.forsy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * The primary entry point for the Bookstore Service Solution application.
 *
 * <p>This class initializes the Spring Boot framework, triggering the
 * auto-configuration, component scanning, and embedded web server setup.
 * It serves as the foundation for the bookstore's infrastructure,
 * orchestrating the integration of security, persistence, and business logic.
 *
 * @author Illia
 */
@SpringBootApplication
@EnableCaching
public class BookStoreServiceSolutionApplication {

  /**
   * Boots the application and starts the Spring application context.
   *
   * @param args command-line arguments passed to the application
   *             during startup
   */
  public static void main(String[] args) {
    SpringApplication.run(BookStoreServiceSolutionApplication.class, args);
  }

}
