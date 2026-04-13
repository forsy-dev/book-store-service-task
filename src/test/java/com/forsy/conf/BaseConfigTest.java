package com.forsy.conf;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;

class BaseConfigTest {

  @Test
  @DisplayName("Annotation [Configuration] exist for BaseConfig class")
  void testsConfigAnnotation() throws ClassNotFoundException {
    Class<?> aClass = Class.forName("com.forsy.conf.BaseConfig");

    assertTrue(aClass.isAnnotationPresent(Configuration.class),
               String.format("Class [%s]. [@Configuration] is missed.", aClass.getSimpleName()));
  }
}
