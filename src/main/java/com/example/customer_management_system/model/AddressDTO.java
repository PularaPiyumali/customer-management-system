package com.example.customer_management_system.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AddressDTO {
    private Long id;
    private String addressLine1;
    private String addressLine2;
    private Long cityId;
    private String cityName;
    private String countryName;
}
