package com.nghoang.banking.service.impl;


import com.nghoang.banking.dto.*;
import com.nghoang.banking.entity.User;
import com.nghoang.banking.repository.UserRepository;
import com.nghoang.banking.utils.AccountUtils;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cglib.core.Local;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Service
@Data
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService{
    UserRepository userRepository;
    EmailService emailService;
    TransactionService transactionService;
    PasswordEncoder passwordEncoder;
    @Override
    public BankResponse createAccount(UserRequest request) {

        if (userRepository.existsByEmailIs(request.getEmail())) {
            return BankResponse.builder()
                    .code(AccountUtils.ACCOUNT_EXISTS_CODE)
                    .message(AccountUtils.ACCOUNT_EXIST_MESSAGE)
                    .accountInfo(null)
                    .build();
        }
        User newUser = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .otherName(request.getOtherName())
                .gender(request.getGender())
                .address(request.getAddress())
                .stateOfOrigin(request.getStateOfOrigin())
                .accountNumber(AccountUtils.generateAccountNumber())
                .accountBalance(BigDecimal.valueOf(0))
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .alternativePhoneNumber(request.getAlternativePhoneNumber())
                .status("ACTIVE")
                .build();
        User savedUser = userRepository.save(newUser);
        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(savedUser.getEmail())
                .subject("ACCOUNT CREATION")
                .messageBody("CONGRATULATION! YOU ACCOUNT HAS BEEN SUCCESSFULLY CREATED!\n Your account Detail: \n Account Name: " + savedUser.getFirstName() + " " + savedUser.getLastName() + " " + savedUser.getOtherName() + "\nAccount number: " + savedUser.getAccountNumber())
                .build();
        emailService.sendEmail(emailDetails);
        return BankResponse.builder()
                .code(AccountUtils.ACCOUNT_CREATION_SUCCESS)
                .message(AccountUtils.ACCOUNT_CREATION_MESSAGE)
                .accountInfo(AccountInfo.builder()
                        .accountName(savedUser.getFirstName() + " " + savedUser.getLastName() + " " + savedUser.getOtherName())
                        .accountBalance(savedUser.getAccountBalance())
                        .accountNumber(savedUser.getAccountNumber())
                        .build())
                .build();
    }

    @Override
    public BankResponse balanceEnquiry(EnquiryRequest request) {
        boolean isAccountExist = userRepository.existsByAccountNumber(request.getAccountNumber());
        if (!isAccountExist) {
            return BankResponse.builder()
                    .code(AccountUtils.ACCOUNT_NOT_EXIST_CODE)
                    .message(AccountUtils.ACCOUNT_NOT_EXIST_MESSAGE)
                    .accountInfo(null)
                    .build();
        }
        User user = userRepository.findByAccountNumber(request.getAccountNumber());
        return BankResponse.builder()
                .code(AccountUtils.ACCOUNT_FOUND_CODE)
                .message(AccountUtils.ACCOUNT_FOUND_SUCCESS)
                .accountInfo(AccountInfo.builder()
                        .accountBalance(user.getAccountBalance())
                        .accountName(user.getFirstName() + " " + user.getLastName() + " " + user.getOtherName())
                        .accountNumber(user.getAccountNumber())
                        .build())
                .build();
    }

    @Override
    public String nameEnquiry(EnquiryRequest request) {
        boolean isAccountExist = userRepository.existsByAccountNumber(request.getAccountNumber());
        if (!isAccountExist) {
            return AccountUtils.ACCOUNT_NOT_EXIST_MESSAGE;
        }
        User user = userRepository.findByAccountNumber(request.getAccountNumber());
        return user.getFirstName() + " " + user.getLastName() + " " + user.getOtherName();
    }

    @Override
    public BankResponse creditAccount(CreditDebitRequest request) {
        boolean isAccountExist = userRepository.existsByAccountNumber(request.getAccountNumber());
        if (!isAccountExist) {
            return BankResponse.builder()
                    .code(AccountUtils.ACCOUNT_NOT_EXIST_CODE)
                    .message(AccountUtils.ACCOUNT_NOT_EXIST_MESSAGE)
                    .accountInfo(null)
                    .build();
        }
        User user = userRepository.findByAccountNumber(request.getAccountNumber());
        user.setAccountBalance(user.getAccountBalance().add(request.getAmount()));
        userRepository.save(user);
        TransactionDto transactionDto = TransactionDto.builder()
                .accountNumber(user.getAccountNumber())
                .transactionType("CREDIT")
                .amount(request.getAmount())
                .status("SUCCESS")
                .build();
        transactionService.saveTransaction(transactionDto);

        return BankResponse.builder()
                .code(AccountUtils.ACCOUNT_CREDITED_SUCCESS)
                .message(AccountUtils.ACCOUNT_CREDITED_SUCCESS_MESSAGE)
                .accountInfo(AccountInfo.builder()
                        .accountName(user.getFirstName()+" "+user.getLastName()+" "+user.getOtherName())
                        .accountBalance(user.getAccountBalance())
                        .accountNumber(request.getAccountNumber())
                        .build())
                .build();
    }

    @Override
    public BankResponse debitAccount(CreditDebitRequest request) {
        boolean isAccountExist = userRepository.existsByAccountNumber(request.getAccountNumber());
        if (!isAccountExist) {
            return BankResponse.builder()
                    .code(AccountUtils.ACCOUNT_NOT_EXIST_CODE)
                    .message(AccountUtils.ACCOUNT_NOT_EXIST_MESSAGE)
                    .accountInfo(null)
                    .build();
        }
        //check if the amount intend to witdraw  not more than amount current
        User user = userRepository.findByAccountNumber(request.getAccountNumber());
        int availableBalance = user.getAccountBalance().intValue();
        int debitAmount = request.getAmount().intValue();
        int sufficient = availableBalance - debitAmount;
        if (sufficient < 0) {
            return BankResponse.builder()
                    .code(AccountUtils.INSUFFICIENT_BALANCE_CODE)
                    .message(AccountUtils.INSUFFICIENT_BALANCE_MESSAGE)
                    .accountInfo(null)
                    .build();
        }
        else {
            user.setAccountBalance(BigDecimal.valueOf(sufficient));
            userRepository.save(user);
            TransactionDto transactionDto = TransactionDto.builder()
                    .accountNumber(user.getAccountNumber())
                    .transactionType("CREDIT")
                    .amount(request.getAmount())
                    .status("SUCCESS")
                    .build();
            transactionService.saveTransaction(transactionDto);
            return BankResponse.builder()
                    .code(AccountUtils.ACCOUNT_DEBITED_SUCCESS)
                    .message(AccountUtils.ACCOUNT_DEBITTED_MESSAGE)
                    .accountInfo(AccountInfo.builder()
                            .accountBalance(user.getAccountBalance())
                            .accountName(user.getFirstName() + " " + user.getLastName() + " " + user.getOtherName())
                            .accountNumber(request.getAccountNumber())
                            .build())
                    .build();
        }
    }

    @Override
    public BankResponse transfer(TransferRequest request) {
        //sourceAccountNumber đã check khi login r nên ko cần check nữa
        boolean isDestinationAccountExist = userRepository.existsByAccountNumber(request.getDestinationAccountNumber());
        if (!isDestinationAccountExist) {
            return BankResponse.builder()
                    .code(AccountUtils.ACCOUNT_NOT_EXIST_CODE)
                    .message(AccountUtils.ACCOUNT_NOT_EXIST_MESSAGE)
                    .accountInfo(null)
                    .build();
        }
        User sourceAccountUser = userRepository.findByAccountNumber(request.getSourceAccountNumber());
        if (sourceAccountUser.getAccountBalance().intValue() - request.getAmount().intValue() < 0) {
            return BankResponse.builder()
                    .code(AccountUtils.INSUFFICIENT_BALANCE_CODE)
                    .message(AccountUtils.INSUFFICIENT_BALANCE_MESSAGE)
                    .accountInfo(null)
                    .build();
        }
        sourceAccountUser.setAccountBalance(sourceAccountUser.getAccountBalance().subtract(request.getAmount()));
        userRepository.save(sourceAccountUser);
        EmailDetails debitAlert = EmailDetails.builder()
                .subject("DEBIT ALERT")
                .recipient(sourceAccountUser.getEmail())
                .messageBody("The sum of " + request.getAmount() + " has been deducted from your account! Your current balance is: " + sourceAccountUser.getAccountBalance())
                .build();
        emailService.sendEmail(debitAlert);

        User destinationAccountUser = userRepository.findByAccountNumber(request.getDestinationAccountNumber());

        destinationAccountUser.setAccountBalance(destinationAccountUser.getAccountBalance().add(request.getAmount()));
        EmailDetails creditAlert = EmailDetails.builder()
                .subject("CREDIT ALERT")
                .recipient(destinationAccountUser.getEmail())
                .messageBody("The sum of " + request.getAmount() + " has been add to your account from " + sourceAccountUser.getAccountNumber() + ". Your current balance is: " + destinationAccountUser.getAccountBalance())
                .build();
        userRepository.save(destinationAccountUser);
        emailService.sendEmail(creditAlert);
        TransactionDto transactionDto = TransactionDto.builder()
                .accountNumber(destinationAccountUser.getAccountNumber())
                .transactionType("CREDIT")
                .amount(request.getAmount())
                .status("SUCCESS")
                .build();
        transactionService.saveTransaction(transactionDto);
        return BankResponse.builder()
                .code(AccountUtils.TRANSFER_SUCCESSFUL_CODE)
                .message(AccountUtils.TRANSFER_SUCCESSFUL_MESSAGE)
                .accountInfo(null)
                .build();


    }
}
