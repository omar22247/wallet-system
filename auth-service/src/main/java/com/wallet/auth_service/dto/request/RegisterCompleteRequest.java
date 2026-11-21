package com.wallet.auth_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterCompleteRequest(
        @NotBlank @Email @Size(max = 255)
        String email,

        @NotBlank @Pattern(regexp = "\\d{6}", message = "must be a 6-digit code")
        String otp,

        @NotBlank @Size(min = 8, max = 72)
        String password,

        @NotBlank @Size(max = 100)
        String fullName
) {
    @Override
    public String toString() {
        return "RegisterCompleteRequest[email=" + email + ", otp=***, password=***, fullName=" + fullName + "]";
    }
}