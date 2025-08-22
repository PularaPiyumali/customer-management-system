package com.example.customer_management_system.controller;

import com.example.customer_management_system.model.BulkUploadResponse;
import com.example.customer_management_system.model.CustomerDTO;
import com.example.customer_management_system.domain.entities.City;
import com.example.customer_management_system.domain.entities.Country;
import com.example.customer_management_system.application.BulkCustomerService;
import com.example.customer_management_system.application.CustomerService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "http://localhost:3000")
@AllArgsConstructor
public class CustomerController {

  private CustomerService customerService;
  private BulkCustomerService bulkCustomerService;

  @PostMapping
  public CustomerDTO createCustomer(@Valid @RequestBody CustomerDTO customerDTO) {
    CustomerDTO createdCustomer = customerService.createCustomer(customerDTO);
    return createdCustomer;
  }

  @GetMapping("/{id}")
  public CustomerDTO getCustomerById(@PathVariable Long id) {
    CustomerDTO customer = customerService.getCustomerById(id);
    return customer;
  }

  @PutMapping("/{id}")
  public CustomerDTO updateCustomer(
      @PathVariable Long id, @Valid @RequestBody CustomerDTO customerDTO) {
    CustomerDTO updatedCustomer = customerService.updateCustomer(id, customerDTO);
    return updatedCustomer;
  }

  @GetMapping("/cities")
  public List<City> getAllCities() {
    List<City> cities = customerService.getAllCities();
    return cities;
  }

  @GetMapping("/countries")
  public List<Country> getAllCountries() {
    List<Country> countries = customerService.getAllCountries();
    return countries;
  }

  @GetMapping
  public Page<CustomerDTO> getAllCustomers(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "id") String sortBy,
      @RequestParam(defaultValue = "asc") String sortDirection) {

    Page<CustomerDTO> customers =
        customerService.getAllCustomers(page, size, sortBy, sortDirection);
    return customers;
  }

  @PostMapping("/bulk-upload")
  public BulkUploadResponse bulkUploadCustomers(@RequestParam("file") MultipartFile file) {
    BulkUploadResponse response = bulkCustomerService.handleBulkUpload(file);
    return response;
  }


  @GetMapping(value = "/bulk-upload/status/{jobId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public BulkUploadResponse getBulkUploadStatus(@PathVariable String jobId) {
    BulkUploadResponse response = bulkCustomerService.getBulkUploadStatus(jobId);
    return response;
  }
}
