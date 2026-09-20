package com.nghoang.banking.service;

import com.nghoang.banking.dto.TransactionDto;
import org.springframework.stereotype.Service;

@Service
public interface TransactionService {
    void saveTransaction(TransactionDto transactionDto);
}
