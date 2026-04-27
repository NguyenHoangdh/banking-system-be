package com.nghoang.banking.service.impl;

import com.nghoang.banking.dto.EmailDetails;

public interface EmailService {
    void sendEmail(EmailDetails emailDetails);
    void sendEmailWithAttachment(EmailDetails emailDetails);
}
