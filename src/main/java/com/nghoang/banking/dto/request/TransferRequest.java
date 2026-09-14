package com.nghoang.banking.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransferRequest {
    @NotBlank(message = "Destination account number has been required")
    String destinationAccountNumber;
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "10000", message = "INVALID_AMOUNT")
    BigDecimal amount;
}
