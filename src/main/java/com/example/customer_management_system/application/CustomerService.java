package com.example.customer_management_system.application;

import com.example.customer_management_system.domain.entities.*;
import com.example.customer_management_system.model.CustomerDTO;
import com.example.customer_management_system.utils.CustomerNotFoundException;
import com.example.customer_management_system.domain.repository.CityRepository;
import com.example.customer_management_system.domain.repository.CountryRepository;
import com.example.customer_management_system.domain.repository.CustomerRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class CustomerService {
  private CustomerRepository customerRepository;
  private CityRepository cityRepository;
  private CountryRepository countryRepository;
  private CustomerValidator customerValidator;
  private CustomerMapper customerMapper;

  /**
   * Create customer customer dto.
   *
   * @param customerDTO the customer dto
   * @return the customer dto
   */
  @Transactional
  public CustomerDTO createCustomer(CustomerDTO customerDTO) {
    customerValidator.validateCustomerCreation(customerDTO);
    Customer customer = customerMapper.toEntity(customerDTO);
    Customer savedCustomer = customerRepository.save(customer);
    log.info("Customer created with NIC {}", savedCustomer.getNicNumber());
    return customerMapper.toDTO(savedCustomer);
  }

  /**
   * Gets customer by id.
   *
   * @param id the id
   * @return the customer by id
   */
  public CustomerDTO getCustomerById(Long id) {
    Customer customer =
        customerRepository
            .findById(id)
            .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));

    return customerMapper.toDTO(customer);
  }


  public Page<CustomerDTO> getAllCustomers(int page, int size, String sortBy, String sortDirection) {
    Sort sort = sortDirection.equalsIgnoreCase("desc")
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();

    Pageable pageable = PageRequest.of(page, size, sort);
    return customerRepository.findAll(pageable).map(customerMapper::toDTO);
  }

  /**
   * Update customer customer dto.
   *
   * @param id the id
   * @param customerDTO the customer dto
   * @return the customer dto
   */
  @Transactional
  public CustomerDTO updateCustomer(Long id, CustomerDTO customerDTO) {
    Customer existingCustomer =
        customerRepository
            .findById(id)
            .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));

    customerValidator.validateCustomerUpdate(customerDTO, id);

    customerMapper.updateEntity(existingCustomer, customerDTO);
    Customer updatedCustomer = customerRepository.save(existingCustomer);
    return customerMapper.toDTO(updatedCustomer);
  }

  /**
   * Gets all cities.
   *
   * @return the all cities
   */
  public List<City> getAllCities() {
    return cityRepository.findAllWithCountry();
  }

  /**
   * Gets all countries.
   *
   * @return the all countries
   */
  public List<Country> getAllCountries() {
    return countryRepository.findAll();
  }
}
