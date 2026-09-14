package com.nghoang.banking.service.impl;


import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.nghoang.banking.dto.EmailDetails;
import com.nghoang.banking.entity.Transaction;
import com.nghoang.banking.entity.User;
import com.nghoang.banking.exception.AppException;
import com.nghoang.banking.exception.ErrorCode;
import com.nghoang.banking.repository.TransactionRepository;
import com.nghoang.banking.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class BankStatement {
    TransactionRepository transactionRepository;
    UserRepository userRepository;
    EmailService emailService;

    private static final String FILE = System.getProperty("java.io.tmpdir") + File.separator + "BankingStatement.pdf";


    public List<Transaction> generateStatement(String startDate, String endDate) {
        User user = userRepository.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        String customerName = (user.getFirstName() + " " + user.getLastName() + " " + Objects.toString(user.getOtherName(),"")).trim();
        LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE);
        LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE);
        List<Transaction> transactionList = transactionRepository.findAllByAccountNumberAndCreateAtBetween(user.getAccountNumber(), start, end);
        Rectangle statementSize = new Rectangle(PageSize.A4);
        Document document = new Document(statementSize, 0,0,0,0);
        log.info("Setting size of document");

        try (OutputStream outputStream = new FileOutputStream(FILE)) {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            PdfPTable bankInfoTable = new PdfPTable(1);
            PdfPCell bankName = new PdfPCell(new Phrase("Banking App"));
            bankName.setBackgroundColor(BaseColor.BLUE);
            bankName.setPadding(20f);

            PdfPCell bankAddress = new PdfPCell(new Phrase("280, An Duong Vuong"));

            bankInfoTable.addCell(bankName);
            bankInfoTable.addCell(bankAddress);

            PdfPTable statementInfo = new PdfPTable(2);
            PdfPCell startDay = new PdfPCell(new Phrase("Start Date: " + startDate));
            PdfPCell statement = new PdfPCell(new Phrase("STATEMENT OF ACCOUNT"));
            PdfPCell stopDate = new PdfPCell(new Phrase("End Date: " + endDate));
            PdfPCell name = new PdfPCell(new Phrase("Customer Name: " + customerName));
            PdfPCell space = new PdfPCell();
            PdfPCell address = new PdfPCell(new Phrase("Customer Address: " + user.getAddress()));

            statementInfo.addCell(startDay);
            statementInfo.addCell(statement);
            statementInfo.addCell(stopDate);
            statementInfo.addCell(name);
            statementInfo.addCell(space);
            statementInfo.addCell(address);


            PdfPTable transactionsTable = new PdfPTable(4);
            PdfPCell date = new PdfPCell(new Phrase("DATE"));
            date.setBackgroundColor(BaseColor.BLUE);
            PdfPCell transactionType = new PdfPCell(new Phrase("TRANSACTION TYPE"));
            transactionType.setBackgroundColor(BaseColor.BLUE);
            PdfPCell transactionAmount = new PdfPCell(new Phrase("TRANSACTION AMOUNT"));
            transactionAmount.setBackgroundColor(BaseColor.BLUE);
            PdfPCell status = new PdfPCell(new Phrase("STATUS"));
            status.setBackgroundColor(BaseColor.BLUE);


            transactionsTable.addCell(date);
            transactionsTable.addCell(transactionType);
            transactionsTable.addCell(transactionAmount);
            transactionsTable.addCell(status);


            transactionList.forEach(transaction -> {
                transactionsTable.addCell(new Phrase(transaction.getCreateAt().toString()));
                transactionsTable.addCell(new Phrase(transaction.getTransactionType()));
                transactionsTable.addCell(new Phrase(transaction.getAmount().toString()));
                transactionsTable.addCell(new Phrase(transaction.getStatus()));
            });


            document.add(bankInfoTable);
            document.add(statementInfo);
            document.add(transactionsTable);

            document.close();
            emailService.sendEmailWithAttachment(EmailDetails.builder()
                    .recipient(user.getEmail())
                    .subject("STATEMENT OF ACCOUNT")
                    .messageBody("Kindly find your requested account statement attached")
                    .attachment(FILE)
                    .build());
        } catch (Exception e) {
            log.error("Failed to generate bank statement for account {}: {}", user.getAccountNumber(), e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
        return transactionList;
    }
}
