package com.nghoang.banking.service.impl;


import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.nghoang.banking.dto.EmailDetails;
import com.nghoang.banking.entity.Transaction;
import com.nghoang.banking.entity.User;
import com.nghoang.banking.repository.TransactionRepository;
import com.nghoang.banking.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class BankStatement {
    TransactionRepository transactionRepository;
    UserRepository userRepository;
    EmailService emailService;
    private static final String FILE = "D:\\DSA\\MyStatement.pdf";

    public List<Transaction> generateStatement(String accountNumber, String startDate, String endDate) throws FileNotFoundException, DocumentException {
        User user = userRepository.findByAccountNumber(accountNumber);
        String customerName = user.getFirstName() + " " + user.getLastName() + " " + user.getOtherName();
        LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE);
        LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE);
        List<Transaction> transactionList = transactionRepository.findAllByAccountNumberAndCreateAtBetween(accountNumber, start, end);
        Rectangle statementSize = new Rectangle(PageSize.A4);
        Document document = new Document(statementSize, 0,0,0,0);
        log.info("Setting size of document");


        OutputStream outputStream = new FileOutputStream(FILE);
        PdfWriter.getInstance(document, outputStream); //getInstance là hàm dùng để lắng nghe mọi thay đổi trên document xong chuyển thành file pdf rồi đẩy vào outputStream
        document.open();

        //thiết kế khuôn
        PdfPTable bankInfoTable = new PdfPTable(1); //tạo bảng này có 1 cột

        //thiết kế viên gạch
        PdfPCell bankName = new PdfPCell(new Phrase("Banking App"));
//        bankName.setBorder(0); //xóa bỏ đường viền của ô
        bankName.setBackgroundColor(BaseColor.BLUE);
        bankName.setPadding(20f);

        PdfPCell bankAddress = new PdfPCell(new Phrase("280, An Duong Vuong"));
//        bankAddress.setBorder(0);

        //đặt gạch vào khuôn
        bankInfoTable.addCell(bankName);
        bankInfoTable.addCell(bankAddress);

        PdfPTable statementInfo = new PdfPTable(2);
        PdfPCell startDay = new PdfPCell(new Phrase("Start Date: " + startDate));
        startDay.setBorder(0);
        PdfPCell statement = new PdfPCell(new Phrase("STATEMENT OF ACCOUNT"));
        statement.setBorder(0);
        PdfPCell stopDate = new PdfPCell(new Phrase("End Date: " + endDate));
        stopDate.setBorder(0);
        PdfPCell name = new PdfPCell(new Phrase("Customer Name: " + customerName));
        name.setBorder(0);
        PdfPCell space = new PdfPCell();
        space.setBorder(0);
        PdfPCell address = new PdfPCell(new Phrase("Customer Address: " + user.getAddress()));
        address.setBorder(0);

        statementInfo.addCell(startDay);
        statementInfo.addCell(statement);
        statementInfo.addCell(stopDate);
        statementInfo.addCell(name);
        statementInfo.addCell(space);
        statementInfo.addCell(address);


        PdfPTable transactionsTable = new PdfPTable(4);
        PdfPCell date = new PdfPCell(new Phrase("DATE"));
        date.setBorder(0);
        date.setBackgroundColor(BaseColor.BLUE);
        PdfPCell transactionType = new PdfPCell(new Phrase("TRANSACTION TYPE"));
        transactionType.setBorder(0);
        transactionType.setBackgroundColor(BaseColor.BLUE);
        PdfPCell transactionAmount = new PdfPCell(new Phrase("TRANSACTION AMOUNT"));
        transactionAmount.setBorder(0);
        transactionAmount.setBackgroundColor(BaseColor.BLUE);
        PdfPCell status = new PdfPCell(new Phrase("STATUS"));
        status.setBackgroundColor(BaseColor.BLUE);
        status.setBorder(0);


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
        return transactionList;
    }
}
