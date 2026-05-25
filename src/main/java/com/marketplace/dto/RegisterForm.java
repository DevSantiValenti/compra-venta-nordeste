package com.marketplace.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterForm {
    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @Email
    @NotBlank
    private String email;

    @Size(min = 6, message = "La clave debe tener al menos 6 caracteres")
    private String password;

    @NotBlank
    private String phone;

    @NotBlank
    private String city;

    @NotBlank
    private String province;
}
