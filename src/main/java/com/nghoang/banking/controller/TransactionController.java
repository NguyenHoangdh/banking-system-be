package com.nghoang.banking.controller;


import com.itextpdf.text.DocumentException;
import com.nghoang.banking.entity.Transaction;
import com.nghoang.banking.service.impl.BankStatement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.util.List;

@RestController
@RequestMapping("/bankStatement")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransactionController {
    BankStatement bankStatement;

    @GetMapping
    public List<Transaction> generateBankStatement(@RequestParam String accountNumber, @RequestParam String startDay, @RequestParam String endDay) throws DocumentException, FileNotFoundException {
        return bankStatement.generateStatement(accountNumber, startDay, endDay);
    }

}
