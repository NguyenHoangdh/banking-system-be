package com.nghoang.banking.service.impl;

import com.nghoang.banking.dto.TransactionDto;
import com.nghoang.banking.entity.Transaction;
import org.springframework.stereotype.Service;

@Service
public interface TransactionService {
    void saveTransaction(TransactionDto transactionDto);
}
