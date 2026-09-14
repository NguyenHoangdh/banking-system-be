package com.nghoang.banking.entity;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // chuỗi ngẫu nhiên 36 ký tự giúp tránh lộ thng tin đã có bao nhiêu giao dịch (nếu dùng ID tự tăng)
    String transactionId;
    String transactionType;
    BigDecimal amount;
    String accountNumber;
    String status;
    @CreationTimestamp
    LocalDateTime createAt; //có giờ phút giây, ko kèm múi giờ (phuf hợp nếu hệ thống dùng trong 1 nội địa), Instant thì lưu chuẩn thời gian UTC
    @UpdateTimestamp
    LocalDateTime modifiedAt;
}
