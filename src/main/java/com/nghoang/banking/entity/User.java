package com.nghoang.banking.entity;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Entity
//ở class entity sẽ lưu xuống database nên dùng riêng getter setter chứ ko nên dùng data, ở dto mới nên dùng data vì ở đó xử lý các tác vụ hệ thống, ở đây nên dùng riêng để tránh làm giảm phần nào đó hiệu suất của hệ thống
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "users")
public class User{


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String firstName;
    String lastName;
    String otherName;
    String gender;
    String address;
    String stateOfOrigin;
    @Column(unique = true)
    String accountNumber;
    BigDecimal accountBalance;
    @Column(unique = true)
    String email;
    String password;
    String phoneNumber;
    String alternativePhoneNumber;
    String status;
    @CreationTimestamp
    LocalDateTime createAt;
    @UpdateTimestamp
    LocalDateTime modifiedAt;

    LocalDate dob;
    @ManyToMany
    Set<Role> roles;
}
