package com.nghoang.banking.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmailDetails {
    String recipient;
    String messageBody;
    String subject;
    String attachment;
}
