package com.example.customer_management_system.domain.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "customers")
public class Customer {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Name is mandatory")
  @Column(nullable = false)
  private String name;

  @NotNull(message = "Date of birth is mandatory")
  @Column(name = "date_of_birth", nullable = false)
  private LocalDate dateOfBirth;

  @NotBlank(message = "NIC number is mandatory")
  @Column(name = "nic_number", nullable = false, unique = true)
  private String nicNumber;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_customer_id")
  @JsonBackReference
  private Customer parentCustomer;

  @OneToMany(mappedBy = "parentCustomer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JsonManagedReference
  private List<Customer> familyMembers = new ArrayList<>();

  @OneToMany(
      mappedBy = "customer",
      cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      orphanRemoval = true)
  @JsonManagedReference
  private Set<MobileNumber> mobileNumbers = new HashSet<>();

  @OneToMany(
      mappedBy = "customer",
      cascade = CascadeType.ALL,
      fetch = FetchType.LAZY,
      orphanRemoval = true)
  @JsonManagedReference
  private Set<Address> addresses = new HashSet<>();

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  public Customer(String name, LocalDate dateOfBirth, String nicNumber) {
    this.name = name;
    this.dateOfBirth = dateOfBirth;
    this.nicNumber = nicNumber;
  }
}
