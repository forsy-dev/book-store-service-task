package com.forsy.conf;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SecurityConfigTest {
    @Test
    @DisplayName("Annotation [Configuration] exist for SecurityConfig class")
    public void testsConfigAnnotation() throws ClassNotFoundException {
        Class<?> aClass = Class.forName("com.forsy.conf.SecurityConfig");

        assertTrue(aClass.isAnnotationPresent(Configuration.class),
                String.format("Class [%s]. [@Configuration] is missed.", aClass.getSimpleName()));
    }
}
