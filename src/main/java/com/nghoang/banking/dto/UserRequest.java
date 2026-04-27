package com.nghoang.banking.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRequest {
    String firstName;
    String lastName;
    String otherName;
    String gender;
    String address;
    String stateOfOrigin;
    String accountNumber;
    String email;
    String password;
    String phoneNumber;
    String alternativePhoneNumber;
}
