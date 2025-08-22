package com.example.customer_management_system.domain.repository;

import com.example.customer_management_system.domain.entities.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
  @Query("SELECT c FROM City c JOIN FETCH c.country")
  List<City> findAllWithCountry();
}
