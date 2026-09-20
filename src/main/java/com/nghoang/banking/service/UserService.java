package com.nghoang.banking.service;

import com.nghoang.banking.dto.AccountInfo;
import com.nghoang.banking.dto.ApiResponse;
import com.nghoang.banking.dto.request.*;
import com.nghoang.banking.dto.response.BankResponse;

import java.util.List;


public interface UserService {
    BankResponse createAccount(UserRequest request);
    BankResponse updateAccount(UserUpdateRequest request);
    ApiResponse<List<AccountInfo>> getAllUsers();
    ApiResponse<List<AccountInfo>> getPage(int size, int page);

    BankResponse balanceEnquiry();
    BankResponse nameEnquiry(EnquiryRequest request);
    BankResponse credit(CreditRequest request);
    BankResponse debit(DebitRequest request);
    BankResponse transfer(TransferRequest request);

}
