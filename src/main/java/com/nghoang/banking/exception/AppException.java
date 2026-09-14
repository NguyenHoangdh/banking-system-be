package com.nghoang.banking.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException{
    ErrorCode errorCode;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage()); //gửi tin nhắn lỗi cho java biết
        this.errorCode = errorCode; //lưu lại nguyên đối tượng errorCode để dùng về sau
    }

}
