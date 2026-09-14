package com.nghoang.banking.dto.request;

import com.nghoang.banking.validator.DobConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRequest {
    @NotBlank(message = "First name is required")
    String firstName;
    @NotBlank(message = "Last name is required")
    String lastName;
    String otherName;
    String gender;
    String address;
    String stateOfOrigin;
    @Email(message = "Email have to contains @")
    @NotBlank(message = "Email can not be blank")
    String email;
    @Size(min = 8, message = "INVALID_PASSWORD")
    String password;
    @NotBlank(message = "Phone number is required")
    String phoneNumber;
    String alternativePhoneNumber;
    @DobConstraint(min = 18, message = "INVALID_DOB")
    LocalDate dob;
}
