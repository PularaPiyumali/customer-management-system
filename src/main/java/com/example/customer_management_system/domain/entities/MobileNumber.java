package com.example.customer_management_system.domain.entities;

import com.example.customer_management_system.domain.entities.Customer;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "mobile_numbers")
public class MobileNumber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonBackReference
    private Customer customer;
    @Column(name = "mobile_number", length = 15, nullable = false)
    private String mobileNumber;
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public MobileNumber(Customer customer, String mobileNumber) {
        this.customer = customer;
        this.mobileNumber = mobileNumber;
    }

}
