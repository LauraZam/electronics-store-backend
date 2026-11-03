package com.example.shop.dto.response;

public record AddressResponse(
        Long id,
        String title,
        String recipientName,
        String phone,
        String country,
        String city,
        String street,
        String zipCode,
        Boolean isDefault
) {}