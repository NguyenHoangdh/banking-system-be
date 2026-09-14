package com.nghoang.banking.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.math.BigDecimal;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountInfo {
    @Schema(
            name = "User Account Name"
    )
    String accountName;
    @Schema(
            name = "User Account Balance"
    )
    BigDecimal accountBalance;
    @Schema(
            name = "User Account Number"
    )
    String accountNumber;
}
