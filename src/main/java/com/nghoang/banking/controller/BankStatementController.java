package com.nghoang.banking.controller;


import com.itextpdf.text.DocumentException;
import com.nghoang.banking.entity.Transaction;
import com.nghoang.banking.service.impl.BankStatement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Bank Statement API", description = "API tạo hóa đơn sao kê ngân hàng")
public class BankStatementController {
    BankStatement bankStatement;

    @GetMapping
    public List<Transaction> generateBankStatement(@RequestParam String startDay, @RequestParam String endDay) throws DocumentException, FileNotFoundException {
        return bankStatement.generateStatement(startDay, endDay);
    }

}
