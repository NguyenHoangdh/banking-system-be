package com.nghoang.banking.controller;


import com.nghoang.banking.dto.AccountInfo;
import com.nghoang.banking.dto.ApiResponse;
import com.nghoang.banking.dto.request.*;
import com.nghoang.banking.dto.response.BankResponse;
import com.nghoang.banking.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Tag(name = "User Account Management APIs", description = "Các API quản lý tài khoản ngân hàng.")
public class UserController {
    UserService userService;

    @PostMapping
    @SecurityRequirements
    @Operation(summary = "Tạo tài khoản", description = "API tạo tài khoản người dùng.")
    public BankResponse create(@RequestBody @Valid UserRequest request) {
        return userService.createAccount(request);
    }

    @Operation(summary = "Cập nhật tài khoản", description = "API cập nhật thông tin tài khoản.")
    @PutMapping
    public BankResponse update(@RequestBody @Valid UserUpdateRequest request) {
        return userService.updateAccount(request);
    }

    @Operation(summary = "Xem số dư", description = "API xem số dư tài khoản.")
    @GetMapping("/balanceEnquiry")
    public BankResponse balanceEnquiry() { //hoặc để trống
        return userService.balanceEnquiry();
    }

    @Operation(summary = "Xem tên tài khoản", description = "API xem tên tài khoản.")
    @GetMapping("/nameEnquiry")
    public BankResponse nameEnquiry(@RequestParam String accountNumber) {
        return  userService.nameEnquiry(EnquiryRequest.builder()
                .accountNumber(accountNumber)
                .build());
    }

    @Operation(summary = "Lấy danh sách khách hàng", description = "API xem danh sách khách hàng của ngân hàng.")
    @GetMapping("/all")
    public ApiResponse<List<AccountInfo>> getUsers() {
        return  userService.getAllUsers();
    }

    @Operation(summary = "Nạp tiền", description = "API thực hiện cộng tiền vào tài khoản.")
    @PostMapping("/credit")
    public BankResponse creditAccount(@RequestBody @Valid CreditRequest request) {
        return  userService.credit(request);
    }

    @PostMapping("/debit")
    @Operation(summary = "Rút tiền", description = "API thực hiện trừ tiền vào tài khoản.")
    public BankResponse debitAccount(@RequestBody @Valid DebitRequest request) {
        return userService.debit(request);
    }


    @PostMapping("/transfer")
    @Operation(summary = "Chuyển tiền", description = "API thực hiện chuyển tiền từ tài khoản chính chủ sang tài khoản người dùng khác.")
    public BankResponse transfer(@RequestBody @Valid TransferRequest request) {
        return userService.transfer(request);
    }
}
