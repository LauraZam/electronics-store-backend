package com.example.shop.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank(message = "Current password is obligatory") String currentPassword,
        @NotBlank(message = "New password is obligatory") String newPassword
) {}