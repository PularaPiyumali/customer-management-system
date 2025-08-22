package com.example.customer_management_system.domain.repository;

import com.example.customer_management_system.domain.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
  Optional<Customer> findByNicNumber(String nicNumber);

  boolean existsByNicNumber(String nicNumber);
}
