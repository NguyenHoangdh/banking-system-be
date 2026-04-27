package com.nghoang.banking.repository;

import com.nghoang.banking.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    List<Transaction> findAllByAccountNumberAndCreateAtBetween(
            String accountNumber,
            LocalDate startDay,
            LocalDate endDay
    );
}
