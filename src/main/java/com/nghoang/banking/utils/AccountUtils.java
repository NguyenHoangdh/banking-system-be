package com.nghoang.banking.utils;

import java.time.Year;
import java.util.Random;

public class AccountUtils {

    public static final String ACCOUNT_CREATION_SUCCESS = "001";
    public static final String ACCOUNT_CREATION_MESSAGE = "Account has been create successfully!";

    public static final String ACCOUNT_FOUND_CODE = "002";
    public static final String ACCOUNT_FOUND_MESSAGE = "User Account Found";
    public static final String ACCOUNT_CREDITED_SUCCESS = "003";
    public static final String ACCOUNT_CREDITED_SUCCESS_MESSAGE = "User account was credited successfully with amount: %,.0f VND";

    public static final String ACCOUNT_DEBITED_SUCCESS= "004";
    public static final String ACCOUNT_DEBITED_MESSAGE = "User account has been debited successfully with amount: %,.0f VND";
    public static final String TRANSFER_SUCCESSFUL_CODE = "005";
    public static final String TRANSFER_SUCCESSFUL_MESSAGE = "Transfer successful";
    public static final String UPDATE_USER_SUCCESSFUL_CODE = "006";

    public static final String UPDATE_USER_SUCCESSFUL_MESSAGE = "Update information successfully";
    public static final String GET_USER_SUCCESS = "007";

    public static final String GET_USER_SUCCESS_MESSAGE = "List user: ";

    /**
     * 2026 + randomSixDigits
     */
    public static String generateAccountNumber() {
        Year currentYear = Year.now();
        int min = 100000;
        int max = 999999;
        int ranNumber = (int) Math.floor(Math.random() * (max - min + 1) + min);
        String year = String.valueOf(currentYear);
        String ranomNumber = String.valueOf(ranNumber);
        StringBuilder accountNumber = new StringBuilder();
        return accountNumber.append(year).append(ranomNumber).toString();
    }
}
