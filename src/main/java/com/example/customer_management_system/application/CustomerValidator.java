package com.example.customer_management_system.application;

import com.example.customer_management_system.domain.entities.Customer;
import com.example.customer_management_system.domain.repository.CustomerRepository;
import com.example.customer_management_system.model.CustomerDTO;
import com.example.customer_management_system.model.FamilyMemberDTO;
import com.example.customer_management_system.utils.DuplicateNicException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class CustomerValidator {

  private final CustomerRepository customerRepository;

  /**
   * Validate customer creation.
   *
   * @param customerDTO the customer dto
   */
  public void validateCustomerCreation(CustomerDTO customerDTO) {
    if (customerRepository.existsByNicNumber(customerDTO.getNicNumber())) {
      throw new DuplicateNicException(
          "Customer with NIC " + customerDTO.getNicNumber() + " already exists");
    }

    if (customerDTO.getFamilyMembers() != null) {
      validateFamilyMembers(customerDTO.getFamilyMembers(), customerDTO.getNicNumber(), null);
    }
  }

  /**
   * Validate customer update.
   *
   * @param customerDTO the customer dto
   * @param parentId the parent id
   */
  public void validateCustomerUpdate(CustomerDTO customerDTO, Long parentId) {
    Optional<Customer> existing = customerRepository.findByNicNumber(customerDTO.getNicNumber());
    if (existing.isPresent() && !existing.get().getId().equals(parentId)) {
      throw new DuplicateNicException(
          "Customer with NIC " + customerDTO.getNicNumber() + " already exists");
    }

    if (customerDTO.getFamilyMembers() != null) {
      validateFamilyMembers(customerDTO.getFamilyMembers(), customerDTO.getNicNumber(), parentId);
    }
  }

  private void validateFamilyMembers(
      List<FamilyMemberDTO> familyMembers, String parentNic, Long parentId) {

    // Check duplicates within the list
    List<String> nicNumbers =
        familyMembers.stream().map(FamilyMemberDTO::getNicNumber).collect(Collectors.toList());

    if (nicNumbers.size() != nicNumbers.stream().distinct().count()) {
      throw new DuplicateNicException("Duplicate NICs found within family members");
    }

    for (FamilyMemberDTO fm : familyMembers) {
      // Family member cannot have same NIC as parent
      if (fm.getNicNumber().equals(parentNic)) {
        throw new DuplicateNicException(
            "Family member cannot have the same NIC as the parent customer: " + fm.getNicNumber());
      }

      Optional<Customer> existingCustomer = customerRepository.findByNicNumber(fm.getNicNumber());
      if (existingCustomer.isPresent()) {
        Customer existing = existingCustomer.get();

        // Allow if already a child of this parent (for updates)
        if (parentId != null
            && existing.getParentCustomer() != null
            && existing.getParentCustomer().getId().equals(parentId)) {
          continue;
        }

        // Allow if standalone customer (no parent yet)
        if (existing.getParentCustomer() == null) {
          continue;
        }

        // Otherwise, duplicate: already child of another parent
        throw new DuplicateNicException(
            "Family member with NIC "
                + fm.getNicNumber()
                + " is already a family member of another customer");
      }
    }
  }
}
