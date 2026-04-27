package com.nghoang.banking.repository;

import com.nghoang.banking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Boolean existsByEmailIs(String email);
    Boolean existsByAccountNumber(String number);

    User findByAccountNumber(String accountNumber);
}
