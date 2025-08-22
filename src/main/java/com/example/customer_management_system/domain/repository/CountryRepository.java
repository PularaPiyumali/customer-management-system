package com.example.customer_management_system.domain.repository;

import com.example.customer_management_system.domain.entities.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {
  Optional<Country> findByName(String name);

  boolean existsByName(String name);
}
