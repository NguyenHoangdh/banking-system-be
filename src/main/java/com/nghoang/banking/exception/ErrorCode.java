package com.nghoang.banking.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum ErrorCode {
    INVALID_KEY(1111, "Invalid key", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1001, "User not existed", HttpStatus.NOT_FOUND),
    ACCOUNT_EXISTED(1002, "Account has been existed", HttpStatus.BAD_REQUEST),
    DESTINATION_ACCOUNT_NOT_EXISTED(1003, "Destination account number is not existed", HttpStatus.BAD_REQUEST),
    ACCOUNT_NOT_ACTIVE(1100, "Account not active", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1006,
            "Unauthenticated", HttpStatus.UNAUTHORIZED),
    AUTHORIZATION_DENIED(1007, "You dont have permission to access this resource!", HttpStatus.FORBIDDEN),
    SELF_TRANSFER_NOT_ALLOWED(1008, "Cannot transfer to your own account", HttpStatus.BAD_REQUEST),
    TRANSFER_CONFLICT(1009, "Transfer failed due to concurrent conflict, please try again", HttpStatus.CONFLICT),
    ROLE_NOT_EXISTED(1010, "Role not existed", HttpStatus.NOT_FOUND),
    INSUFFICIENT_BALANCE_CODE(1011, "Account balance not sufficient", HttpStatus.BAD_REQUEST),
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized exception", HttpStatus.INTERNAL_SERVER_ERROR),
    ROLE_EXISTED(1013, "Role has been existed", HttpStatus.BAD_REQUEST),
    PERMISSION_EXISTED(1031, "Permission has been existed", HttpStatus.BAD_REQUEST),
    PERMISSION_NOT_EXISTED(1032, "Permission not existed", HttpStatus.BAD_REQUEST),
    INVALID_AMOUNT(1015, "Amount must be at least {value} VND", HttpStatus.BAD_REQUEST),
    ATTACHMENT_NOT_FOUND(1016, "Not found any attachment", HttpStatus.NOT_FOUND),
    DATA_EXISTED_IN_ROLE(1017, "Permission has existed", HttpStatus.BAD_REQUEST),
    INVALID_DOB(1018, "Client must be at least {min} years old", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1019, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST)

    ;
    int code;
    String message;
    HttpStatusCode httpStatusCode;
}
