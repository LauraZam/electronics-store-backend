package com.example.shop.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AddressRequest(
        @NotBlank(message = "Title is required (e.g. Home, Work)") String title,
        @NotBlank(message = "Recipient name is required") String recipientName,
        @NotBlank(message = "Phone is required") String phone,
        @NotBlank(message = "Country is required") String country,
        @NotBlank(message = "City is required") String city,
        @NotBlank(message = "Street address is required") String street,
        String zipCode,
        Boolean isDefault
) {}