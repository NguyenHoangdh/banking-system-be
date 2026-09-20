package com.nghoang.banking.event.listener;

import com.nghoang.banking.dto.EmailDetails;
import com.nghoang.banking.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Component
@RequiredArgsConstructor
public class EmailServiceListener {
    private final EmailService emailService;
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSendEmail(EmailDetails emailDetails) {
        emailService.sendEmail(emailDetails);
    }
}