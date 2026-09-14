package com.nghoang.banking.dto.response;

import com.nghoang.banking.dto.AccountInfo;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BankResponse {
    String code;
    String message;
    AccountInfo accountInfo;
}
