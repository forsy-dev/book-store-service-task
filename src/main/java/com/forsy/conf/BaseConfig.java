package com.forsy.conf;

import com.forsy.dto.BookItemDto;
import com.forsy.dto.OrderDto;
import com.forsy.model.BookItem;
import com.forsy.model.Order;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for core application utilities and beans.
 *
 * <p>Provides centralized instantiation and configuration for essential
 * third-party libraries used throughout the application architecture.
 *
 * @author Illia
 */
@Configuration
public class BaseConfig {

  /**
   * Configures and provides a singleton {@link ModelMapper} instance for the Spring context.
   *
   * <p>The mapper is customized with specific type maps to handle complex
   * entity-to-DTO conversions. It defines explicit property mappings to extract
   * nested entity fields, such as flattening client/employee emails for orders
   * and extracting book names for book items.
   *
   * @return a fully configured {@link ModelMapper} ready for dependency injection
   */
  @Bean
  public ModelMapper modelMapper() {
    ModelMapper modelMapper = new ModelMapper();

    modelMapper.createTypeMap(Order.class, OrderDto.class)
        .addMappings(mapper -> {
          mapper.map(src -> src.getClient().getEmail(), OrderDto::setClientEmail);
          mapper.map(src -> src.getEmployee().getEmail(), OrderDto::setEmployeeEmail);
        });

    modelMapper.createTypeMap(BookItem.class, BookItemDto.class)
        .addMappings(mapper ->
            mapper.map(src -> src.getBook().getName(), BookItemDto::setBookName));

    return modelMapper;
  }
}
