package com.nghoang.banking.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Entity
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
//keep lại những token đã logout
public class InvalidatedToken {
    @Id
    String id; //claim jit của token
    Date expiryTime; //keep tgian để khi đạt expiration time thì ra remove nó (dùng những job chạy định kì 1 ngày/lần)
}
