package com.nghoang.banking.repository;

import com.nghoang.banking.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Boolean existsByEmailIs(String email);
    Boolean existsByAccountNumber(String number);

    Optional<User> findByAccountNumber(String accountNumber);


    Optional<User> findUserByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.email = :email")
    //SELECT * FROM users WHERE email = 'hoang@gmail.com' FOR UPDATE; lock để thực hiện phần for update
    Optional<User> findUserByEmailWithLock(@Param("email") String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.accountNumber = :accountNumber")
    Optional<User> findByAccountNumberWithLock(@Param("accountNumber") String accountNumber);
}
