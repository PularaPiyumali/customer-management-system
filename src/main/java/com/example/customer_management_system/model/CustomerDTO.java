package com.example.customer_management_system.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CustomerDTO {

    private Long id;
    @NotBlank(message = "Name is mandatory")
    private String name;
    @NotNull(message = "Date of birth is mandatory")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;
    @NotBlank(message = "NIC number is mandatory")
    private String nicNumber;
    private List<String> mobileNumbers;
    private List<AddressDTO> addresses;
    private List<FamilyMemberDTO> familyMembers;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public CustomerDTO(String name, LocalDate dateOfBirth, String nicNumber) {
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.nicNumber = nicNumber;
    }
}
