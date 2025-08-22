package com.example.customer_management_system.application;

import com.example.customer_management_system.domain.entities.Address;
import com.example.customer_management_system.domain.entities.City;
import com.example.customer_management_system.domain.entities.Customer;
import com.example.customer_management_system.domain.entities.MobileNumber;
import com.example.customer_management_system.domain.repository.CityRepository;
import com.example.customer_management_system.domain.repository.CustomerRepository;
import com.example.customer_management_system.model.AddressDTO;
import com.example.customer_management_system.model.CustomerDTO;
import com.example.customer_management_system.model.FamilyMemberDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class CustomerMapper {

  private CityRepository cityRepository;
  private CustomerRepository customerRepository;

  /** Convert DTO -> Entity */
  public Customer toEntity(CustomerDTO customerDTO) {
    Customer customer = new Customer();
    updateEntity(customer, customerDTO);
    return customer;
  }

  /**
   * Update entity.
   *
   * @param customer the customer
   * @param customerDTO the customer dto
   */
  public void updateEntity(Customer customer, CustomerDTO customerDTO) {
    customer.setName(customerDTO.getName());
    customer.setDateOfBirth(customerDTO.getDateOfBirth());
    customer.setNicNumber(customerDTO.getNicNumber());

    // Mobile numbers
    customer.getMobileNumbers().clear();
    if (customerDTO.getMobileNumbers() != null) {
      Set<MobileNumber> mobiles =
          customerDTO.getMobileNumbers().stream()
              .map(number -> new MobileNumber(customer, number))
              .collect(Collectors.toSet());
      customer.setMobileNumbers(mobiles);
    }

    // Addresses
    customer.getAddresses().clear();
    if (customerDTO.getAddresses() != null) {
      Set<Address> addresses =
          customerDTO.getAddresses().stream()
              .map(
                  addressDTO -> {
                    City city =
                        cityRepository
                            .findById(addressDTO.getCityId())
                            .orElseThrow(() -> new RuntimeException("City not found"));
                    return new Address(
                        customer, addressDTO.getAddressLine1(), addressDTO.getAddressLine2(), city);
                  })
              .collect(Collectors.toSet());
      customer.setAddresses(addresses);
    }

    // Family members
    customer.getFamilyMembers().clear();
    if (customerDTO.getFamilyMembers() != null) {
      List<Customer> familyMembers = new ArrayList<>();
      for (FamilyMemberDTO fm : customerDTO.getFamilyMembers()) {
        // Check if family member already exists as a customer
        Customer existingFamilyMember =
            customerRepository.findByNicNumber(fm.getNicNumber()).orElse(null);
        if (existingFamilyMember != null) {
          // Existing customer becomes the parent of this new customer
          existingFamilyMember.getFamilyMembers().add(customer); // optional if bi-directional
          customer.setParentCustomer(existingFamilyMember);
          continue;
        }
        // Create new family member
        Customer familyMember = new Customer();
        familyMember.setName(fm.getFamilyMemberName());
        familyMember.setNicNumber(fm.getNicNumber());
        familyMember.setDateOfBirth(fm.getDateOfBirth());
        familyMember.setParentCustomer(customer);
        familyMembers.add(familyMember);
      }
      customer.setFamilyMembers(familyMembers);
    }
  }

  /** Convert Entity -> DTO */
  public CustomerDTO toDTO(Customer customer) {
    CustomerDTO customerDTO = new CustomerDTO();
    customerDTO.setId(customer.getId());
    customerDTO.setName(customer.getName());
    customerDTO.setDateOfBirth(customer.getDateOfBirth());
    customerDTO.setNicNumber(customer.getNicNumber());
    customerDTO.setCreatedAt(customer.getCreatedAt());
    customerDTO.setUpdatedAt(customer.getUpdatedAt());

    // Mobile numbers
    if (customer.getMobileNumbers() != null) {
      List<String> mobileNumbers =
          customer.getMobileNumbers().stream()
              .map(MobileNumber::getMobileNumber)
              .collect(Collectors.toList());
      customerDTO.setMobileNumbers(mobileNumbers);
    }

    // Addresses
    if (customer.getAddresses() != null) {
      List<AddressDTO> addresses =
          customer.getAddresses().stream()
              .map(
                  address -> {
                    AddressDTO addressDTO = new AddressDTO();
                    addressDTO.setId(address.getId());
                    addressDTO.setAddressLine1(address.getAddressLine1());
                    addressDTO.setAddressLine2(address.getAddressLine2());
                    addressDTO.setCityId(address.getCity().getId());
                    addressDTO.setCityName(address.getCity().getName());
                    addressDTO.setCountryName(address.getCity().getCountry().getName());
                    return addressDTO;
                  })
              .collect(Collectors.toList());
      customerDTO.setAddresses(addresses);
    }

    // Family members
    if (customer.getFamilyMembers() != null) {
      List<FamilyMemberDTO> familyMembers =
          customer.getFamilyMembers().stream()
              .map(fm -> new FamilyMemberDTO(fm.getName(), fm.getNicNumber(), fm.getDateOfBirth()))
              .collect(Collectors.toList());
      customerDTO.setFamilyMembers(familyMembers);
    }

    return customerDTO;
  }
}
