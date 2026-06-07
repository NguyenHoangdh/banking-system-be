package com.nghoang.banking.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreditDebitRequest {
    String accountNumber;
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "10000", message = "Amount must be at least 10000")
    BigDecimal amount;
}
