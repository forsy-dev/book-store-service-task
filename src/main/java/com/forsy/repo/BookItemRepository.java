package com.forsy.repo;

import com.forsy.model.BookItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing persistent {@link BookItem} entities.
 *
 * <p>This interface serves as the data access layer for order line items,
 * providing standard CRUD operations and a bridge to the underlying
 * relational database. It abstracts the complexity of SQL interactions
 * through the Spring Data JPA framework.
 *
 * @author Illia
 */
@Repository
public interface BookItemRepository extends JpaRepository<BookItem, Long> {
}
