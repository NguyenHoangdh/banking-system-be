package com.nghoang.banking.service.impl;

import com.nghoang.banking.dto.AccountInfo;
import com.nghoang.banking.dto.ApiResponse;
import com.nghoang.banking.dto.EmailDetails;
import com.nghoang.banking.dto.TransactionDto;
import com.nghoang.banking.dto.request.*;
import com.nghoang.banking.dto.response.BankResponse;
import com.nghoang.banking.entity.User;
import com.nghoang.banking.event.event.TransferEmailEvent;
import com.nghoang.banking.exception.AppException;
import com.nghoang.banking.exception.ErrorCode;
import com.nghoang.banking.mapper.UserMapper;
import com.nghoang.banking.repository.RoleRepository;
import com.nghoang.banking.repository.UserRepository;
import com.nghoang.banking.utils.AccountUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    TransactionService transactionService;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;
    ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public BankResponse createAccount(UserRequest request) {
        if (userRepository.existsByEmailIs(request.getEmail())) {
            throw new AppException(ErrorCode.ACCOUNT_EXISTED);
        }
        User user = userMapper.toUser(request);
        String accountNumber;
        do {
            accountNumber = AccountUtils.generateAccountNumber();
        } while (userRepository.existsByAccountNumber(accountNumber));

        user.setAccountNumber(accountNumber);
        user.setAccountBalance(BigDecimal.valueOf(0));
        user.setStatus("ACTIVE");
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        var userRole = roleRepository.findById("USER").orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
        user.setRoles(new HashSet<>(Set.of(userRole)));
        User savedUser = userRepository.save(user);
        String accountName = buildAccountName(savedUser);
        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(savedUser.getEmail())
                .subject("ACCOUNT CREATION")
                .messageBody("CONGRATULATION! YOU ACCOUNT HAS BEEN SUCCESSFULLY CREATED!\n Your account Detail: \n Account Name: " + accountName + "\nAccount number: " + savedUser.getAccountNumber())
                .build();
        applicationEventPublisher.publishEvent(emailDetails);
        return BankResponse.builder()
                .code(AccountUtils.ACCOUNT_CREATION_SUCCESS)
                .message(AccountUtils.ACCOUNT_CREATION_MESSAGE)
                .accountInfo(AccountInfo.builder()
                        .accountName(accountName)
                        .accountBalance(savedUser.getAccountBalance())
                        .accountNumber(savedUser.getAccountNumber())
                        .build())
                .build();
    }
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<AccountInfo>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<AccountInfo> accountInfos = users.stream().map(userMapper::toAccountInfo).toList();

        return ApiResponse.<List<AccountInfo>>builder()
                .code(Integer.parseInt(AccountUtils.GET_USER_SUCCESS))
                .message(AccountUtils.GET_USER_SUCCESS_MESSAGE)
                .result(accountInfos)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<AccountInfo>> getPage(int page, int size) {
        List<User> users = userRepository.findAll();
        List<AccountInfo> accountInfos = users.stream().map(userMapper::toAccountInfo).toList();

        return ApiResponse.<List<AccountInfo>>builder()
                .code(Integer.parseInt(AccountUtils.GET_USER_SUCCESS))
                .message(AccountUtils.GET_USER_SUCCESS_MESSAGE)
                .result(accountInfos)
                .build();
    }

    @Override
    @Transactional
    public BankResponse updateAccount(UserUpdateRequest request) {
        User user = userRepository.findUserByEmailWithLock(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        userMapper.updateUser(user, request);
        if (request.getPassword()!=null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        userRepository.save(user);
        return BankResponse.builder()
                .code(AccountUtils.UPDATE_USER_SUCCESSFUL_CODE)
                .message(AccountUtils.UPDATE_USER_SUCCESSFUL_MESSAGE)
                .accountInfo(AccountInfo.builder()
                        .accountName(buildAccountName(user))
                        .accountBalance(user.getAccountBalance())
                        .accountNumber(user.getAccountNumber())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BankResponse balanceEnquiry() {
        User user = userRepository.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if (!user.getStatus().equals("ACTIVE")) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_ACTIVE);
        }
        return BankResponse.builder()
                .code(AccountUtils.ACCOUNT_FOUND_CODE)
                .message(AccountUtils.ACCOUNT_FOUND_MESSAGE)
                .accountInfo(AccountInfo.builder()
                        .accountNumber(user.getAccountNumber())
                        .accountName(buildAccountName(user))
                        .accountBalance(user.getAccountBalance())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BankResponse nameEnquiry(EnquiryRequest request) {
        User user = userRepository.findByAccountNumber(request.getAccountNumber()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if (!user.getStatus().equals("ACTIVE")) throw new AppException(ErrorCode.ACCOUNT_NOT_ACTIVE);
        return BankResponse.builder()
                .code(AccountUtils.ACCOUNT_FOUND_CODE)
                .message(AccountUtils.ACCOUNT_FOUND_MESSAGE)
                .accountInfo(AccountInfo.builder()
                        .accountName(buildAccountName(user))
                        .build())
                .build();

    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public BankResponse credit(CreditRequest request) {
        User user = userRepository.findByAccountNumberWithLock(request.getAccountNumber()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if (!user.getStatus().equals("ACTIVE")) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_ACTIVE);
        }
        user.setAccountBalance(user.getAccountBalance().add(request.getAmount()));
        userRepository.save(user);

        TransactionDto transactionDto = TransactionDto.builder()
                .accountNumber(user.getAccountNumber())
                .transactionType("CREDIT")
                .amount(request.getAmount())
                .status("SUCCESS")
                .build();
        transactionService.saveTransaction(transactionDto);
        EmailDetails emailDetails = EmailDetails.builder()
                .subject("ADD MONEY")
                .recipient(user.getEmail())
                .messageBody("Your account has been add " + request.getAmount() + ". Your account balance currently is: " + user.getAccountBalance())
                .build();
        applicationEventPublisher.publishEvent(emailDetails);
        return BankResponse.builder()
                .code(AccountUtils.ACCOUNT_CREDITED_SUCCESS)
                .message(String.format(AccountUtils.ACCOUNT_CREDITED_SUCCESS_MESSAGE, request.getAmount()))
                .accountInfo(AccountInfo.builder()
                        .accountName(buildAccountName(user))
                        .accountBalance(user.getAccountBalance())
                        .accountNumber(user.getAccountNumber())
                        .build())
                .build();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public BankResponse debit(DebitRequest request) {
        User user = userRepository.findByAccountNumberWithLock(request.getAccountNumber()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if (!user.getStatus().equals("ACTIVE")) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_ACTIVE);
        }

        BigDecimal availableBalance = user.getAccountBalance();
        BigDecimal debitAmount = request.getAmount();
        if (availableBalance.compareTo(debitAmount) < 0) {
            throw new AppException(ErrorCode.INSUFFICIENT_BALANCE_CODE);
        }
        user.setAccountBalance(availableBalance.subtract(debitAmount));
        userRepository.save(user);

        TransactionDto transactionDto = TransactionDto.builder()
                .accountNumber(user.getAccountNumber())
                .transactionType("DEBIT")
                .amount(request.getAmount())
                .status("SUCCESS")
                .build();
        transactionService.saveTransaction(transactionDto);
        EmailDetails emailDetails = EmailDetails.builder()
                .subject("SUBTRACT MONEY")
                .recipient(user.getEmail())
                .messageBody("Your account has been deducted " + request.getAmount() + " .Your account balance currently is: " + user.getAccountBalance())
                .build();
        applicationEventPublisher.publishEvent(emailDetails);
        return BankResponse.builder()
                .code(AccountUtils.ACCOUNT_DEBITED_SUCCESS)
                .message(String.format(AccountUtils.ACCOUNT_DEBITED_MESSAGE, request.getAmount()))
                .accountInfo(AccountInfo.builder()
                        .accountBalance(user.getAccountBalance())
                        .accountName(buildAccountName(user))
                        .accountNumber(user.getAccountNumber())
                        .build())
                .build();
    }

    @Override
    @Transactional
    public BankResponse transfer(TransferRequest request) {
        User sourceAccountUser = userRepository.findUserByEmailWithLock(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if (!sourceAccountUser.getStatus().equals("ACTIVE")) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_ACTIVE);
        }
        String sourceAccountNumber = sourceAccountUser.getAccountNumber();
        if (sourceAccountNumber.equals(request.getDestinationAccountNumber())) {
            throw new AppException(ErrorCode.SELF_TRANSFER_NOT_ALLOWED);
        }
        if (sourceAccountUser.getAccountBalance().compareTo(request.getAmount()) < 0) {
            throw new AppException(ErrorCode.INSUFFICIENT_BALANCE_CODE);
        }
        sourceAccountUser.setAccountBalance(sourceAccountUser.getAccountBalance().subtract(request.getAmount()));
        EmailDetails debitAlert = EmailDetails.builder()
                .subject("DEBIT ALERT")
                .recipient(sourceAccountUser.getEmail())
                .messageBody("The sum of " + request.getAmount() + " has been deducted from your account! Your current balance is: " + sourceAccountUser.getAccountBalance())
                .build();
        TransactionDto transactionDto1 = TransactionDto.builder()
                .accountNumber(sourceAccountUser.getAccountNumber())
                .transactionType("DEBIT")
                .amount(request.getAmount())
                .status("SUCCESS")
                .build();

        User destinationAccountUser = userRepository.findByAccountNumberWithLock(request.getDestinationAccountNumber()).orElseThrow(() -> new AppException(ErrorCode.DESTINATION_ACCOUNT_NOT_EXISTED));
        if (!destinationAccountUser.getStatus().equals("ACTIVE")) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_ACTIVE);
        }
        destinationAccountUser.setAccountBalance(destinationAccountUser.getAccountBalance().add(request.getAmount()));
        EmailDetails creditAlert = EmailDetails.builder()
                .subject("CREDIT ALERT")
                .recipient(destinationAccountUser.getEmail())
                .messageBody("The sum of " + request.getAmount() + " has been add to your account from " + sourceAccountUser.getAccountNumber() + ". Your current balance is: " + destinationAccountUser.getAccountBalance())
                .build();
        userRepository.save(destinationAccountUser);
        userRepository.save(sourceAccountUser);


        TransactionDto transactionDto2 = TransactionDto.builder()
                .accountNumber(destinationAccountUser.getAccountNumber())
                .transactionType("CREDIT")
                .amount(request.getAmount())
                .status("SUCCESS")
                .build();
        transactionService.saveTransaction(transactionDto1);
        transactionService.saveTransaction(transactionDto2);
        applicationEventPublisher.publishEvent(new TransferEmailEvent(this, debitAlert, creditAlert));
        return BankResponse.builder()
                .code(AccountUtils.TRANSFER_SUCCESSFUL_CODE)
                .message(AccountUtils.TRANSFER_SUCCESSFUL_MESSAGE)
                .accountInfo(AccountInfo.builder()
                        .accountNumber(sourceAccountNumber)
                        .accountName(buildAccountName(sourceAccountUser))
                        .accountBalance(sourceAccountUser.getAccountBalance())
                        .build())
                .build();
    }
    private String buildAccountName(User user) {
        String name = user.getFirstName() + " " + user.getLastName();
        String other = user.getOtherName();
        return (other == null || other.isBlank()) ? name : name + " " + other
                .trim();
    }

}
