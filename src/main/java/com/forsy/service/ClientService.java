package com.forsy.service;

import com.forsy.dto.AddBalanceDto;
import com.forsy.dto.ChangePasswordDto;
import com.forsy.dto.ClientCreateDto;
import com.forsy.dto.ClientDisplayDto;
import com.forsy.dto.ClientUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface defining the business logic for managing bookstore clients.
 *
 * <p>This interface serves as the authoritative layer for customer-related
 * operations, orchestrating interactions between core client data, security
 * credentials, financial balances, and administrative blocking statuses.
 * It ensures that all client interactions are validated, localized, and
 * synchronized across multiple data repositories.
 *
 * @author Illia
 */
public interface ClientService {

  /**
   * Retrieves a paginated list of clients, optionally filtered by a search keyword.
   *
   * <p>The search logic typically spans both client names and email addresses,
   * performing case-insensitive matching to facilitate administrative discovery.
   *
   * @param pageable the pagination and sorting parameters
   * @param keyword  the search term used to match client profiles
   * @return a {@link Page} of {@link ClientDisplayDto} objects containing profile details
   *     and block status
   */
  Page<ClientDisplayDto> getAllClients(Pageable pageable, String keyword);

  /**
   * Locates a specific client using their unique email address.
   *
   * @param email the unique email of the client to retrieve
   * @return the {@link ClientDisplayDto} representing the found client
   * @throws com.forsy.exception.NotFoundException if no client exists with the given email
   */
  ClientDisplayDto getClientByEmail(String email);

  /**
   * Updates the profile information of an existing client.
   *
   * <p>Modifies basic client metadata while maintaining the integrity of
   * unique identifiers like the email address.
   *
   * @param email     the current email of the client to be modified
   * @param clientDto the data transfer object containing updated profile data
   * @return the updated {@link ClientDisplayDto}
   * @throws com.forsy.exception.NotFoundException if the client is not found
   */
  ClientDisplayDto updateClientByEmail(String email, ClientUpdateDto clientDto);

  /**
   * Permanently removes a client account and all associated records from the system.
   *
   * @param email the unique email of the client to be deleted
   * @throws com.forsy.exception.NotFoundException if the client or their status
   *                                               record does not exist
   */
  void deleteClientByEmail(String email);

  /**
   * Registers a new client into the bookstore with a secured identity.
   *
   * <p>Handles password encoding and initializes the client with a zero
   * balance and a fresh administrative block status record.
   *
   * @param clientDto the data transfer object containing registration details
   * @return the {@link ClientDisplayDto} of the newly created client
   * @throws com.forsy.exception.AlreadyExistException if the email is already assigned to a
   *                                                   client, employee, or block record
   */
  ClientDisplayDto addClient(ClientCreateDto clientDto);

  /**
   * Updates the security credentials for a specific client.
   *
   * <p>Verifies the validity of the current password before encoding
   * and persisting the new credential.
   *
   * @param email the unique email of the client
   * @param dto   the data transfer object containing current and new passwords
   * @throws com.forsy.exception.NotFoundException        if the client is not found
   * @throws com.forsy.exception.InvalidPasswordException if the current password does not match
   */
  void changePassword(String email, ChangePasswordDto dto);

  /**
   * Increases the financial treasury of a client's account.
   *
   * @param email the unique email of the client
   * @param dto   the data transfer object specifying the amount to be added
   * @return the updated {@link ClientDisplayDto} reflecting the new balance
   * @throws com.forsy.exception.NotFoundException if the client is not found
   */
  ClientDisplayDto addBalanceToClient(String email, AddBalanceDto dto);

  /**
   * Imposes a block on a client, restricting access to bookstore features.
   *
   * @param email the unique email of the client to be blocked
   * @throws com.forsy.exception.NotFoundException if the client's status record is not found
   */
  void blockClient(String email);

  /**
   * Removes a previously imposed block, restoring full access to the client.
   *
   * @param email the unique email of the client to be unblocked
   * @throws com.forsy.exception.NotFoundException if the client's status record is not found
   */
  void unblockClient(String email);
}
