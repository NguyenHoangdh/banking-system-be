package com.nghoang.banking.service;

import com.nghoang.banking.dto.EmailDetails;

public interface EmailService {
    void sendEmail(EmailDetails emailDetails);
    void sendEmailWithAttachment(EmailDetails emailDetails);
}
