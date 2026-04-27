package com.nghoang.banking.service.impl;

import com.nghoang.banking.dto.*;


public interface UserService {
    BankResponse createAccount(UserRequest request);
    BankResponse balanceEnquiry(EnquiryRequest request);
    String nameEnquiry(EnquiryRequest request);
    BankResponse creditAccount(CreditDebitRequest request);
    BankResponse debitAccount(CreditDebitRequest request);
    BankResponse transfer(TransferRequest request);

}
