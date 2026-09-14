package com.nghoang.banking.dto.request;

import com.nghoang.banking.validator.DobConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    String address;
    String stateOfOrigin;
    @Email(message = "Email have to contains @")
    String email;
    @Size(min = 8, message = "Password must be at least 8 characters")
    String password;
    String phoneNumber;
    String alternativePhoneNumber;
    @DobConstraint(min = 18)
    LocalDate dob;
}
