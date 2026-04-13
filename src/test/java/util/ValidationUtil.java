package util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.validation.ConstraintViolation;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for common validation-related assertions in test suites.
 *
 * <p>This class provides centralized logic for verifying that the set of
 * validation constraints triggered during a test matches the expected
 * business rules.
 *
 * @author Illia
 */
public final class ValidationUtil {

  /**
   * Private constructor to prevent instantiation of this utility class.
   */
  private ValidationUtil() {
  }

  /**
   * Validates that the message templates from a set of constraint violations
   * match the expected templates exactly.
   *
   * @param <T>               the type of the object being validated
   * @param violations        the set of actual violations produced by the validator
   * @param expectedTemplates the set of message template strings expected
   *                          to be present
   */
  public static <T> void validateMessageTemplates(
      Set<ConstraintViolation<T>> violations,
      Set<String> expectedTemplates) {

    Set<String> actualTemplates = violations.stream()
        .map(ConstraintViolation::getMessageTemplate)
        .collect(Collectors.toSet());

    assertEquals(expectedTemplates, actualTemplates,
                 "Validation messages did not match expected values");
  }
}
