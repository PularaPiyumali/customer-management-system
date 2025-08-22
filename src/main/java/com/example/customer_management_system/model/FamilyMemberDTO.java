package com.example.customer_management_system.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FamilyMemberDTO {
    private String familyMemberName;
    private String nicNumber;
    private LocalDate dateOfBirth;
}
