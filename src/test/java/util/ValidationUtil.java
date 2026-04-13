package util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.validation.ConstraintViolation;
import java.util.Set;
import java.util.stream.Collectors;

public final class ValidationUtil {
  private ValidationUtil() {
  }

  public static <T> void validateMessageTemplates(Set<ConstraintViolation<T>> violations, Set<String> expectedTemplates) {
    Set<String> actualTemplates = violations.stream()
        .map(ConstraintViolation::getMessageTemplate)
        .collect(Collectors.toSet());

    assertEquals(expectedTemplates, actualTemplates, "Validation messages did not match expected values");
  }
}
