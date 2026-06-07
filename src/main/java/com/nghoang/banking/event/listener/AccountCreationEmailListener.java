package com.nghoang.banking.event;

import com.nghoang.banking.service.impl.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Component
@RequiredArgsConstructor
public class AccountCreationEmailListener {
    private final EmailService emailService;
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCreationCompleted(AccountCreationEmailEvent creationEmailEvent) {
        emailService.sendEmail(creationEmailEvent.getCreationAccountAlert());
    }
}
