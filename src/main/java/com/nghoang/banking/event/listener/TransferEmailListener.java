package com.nghoang.banking.event.listener;

import com.nghoang.banking.event.event.TransferEmailEvent;
import com.nghoang.banking.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Component
@RequiredArgsConstructor
public class TransferEmailListener {
    private final EmailService emailService;


    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTransferCompleted(TransferEmailEvent transferEmailEvent) {
        emailService.sendEmail(transferEmailEvent.getCreditAlert());
        emailService.sendEmail(transferEmailEvent.getDebitAlert());
    }
}
