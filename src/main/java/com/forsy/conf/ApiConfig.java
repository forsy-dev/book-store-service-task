package com.forsy.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for external API communications.
 *
 * <p>Provides a centralized {@link RestTemplate} bean to be used across the application
 * for making synchronous HTTP requests to external services or microservices.
 *
 * @author Illia
 */
@Configuration
public class ApiConfig {

  /**
   * Creates a single instance of {@link RestTemplate} for the Spring application context.
   *
   * <p>This bean can be injected into any service that requires HTTP client capabilities.
   *
   * @return a newly instantiated {@link RestTemplate}
   */
  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }
}
