package com.forsy.model.enums;

/**
 * Represents the distinct security and authorization roles a user can possess within the system.
 *
 * <p>These roles control access levels to different application endpoints, resources, and
 * business features.
 * </p>
 */
public enum Role {

  /**
   * Internal staff role with administrative and management access privileges.
   */
  EMPLOYEE,

  /**
   * External customer role with standard access restricted to consumer-level services.
   */
  CLIENT
}
