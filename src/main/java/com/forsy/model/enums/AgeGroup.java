package com.forsy.model.enums;

/**
 * Enumeration representing the intended audience age categories for books.
 *
 * <p>This classification is used to enforce age-based content restrictions
 * and to help clients find literature appropriate for their developmental
 * stage or reading level.
 *
 * @author Illia
 */
public enum AgeGroup {

  /**
   * Content specifically designed for children, typically under 12 years of age.
   */
  CHILD,

  /**
   * Content intended for teenagers and young adults, typically ages 13 to 17.
   */
  TEEN,

  /**
   * General content intended for mature readers aged 18 and above.
   */
  ADULT,

  /**
   * Miscellaneous or specialized content that does not fit into standard age brackets.
   */
  OTHER
}
