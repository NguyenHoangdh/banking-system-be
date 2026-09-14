package com.nghoang.banking.event.listener;

import com.nghoang.banking.dto.EmailDetails;
import com.nghoang.banking.service.impl.EmailService;
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
//   Tầng Service: Đã làm xong toàn bộ việc "soạn thảo văn bản" (bao gồm nội dung cộng/trừ tiền).
//  Class EmailDetails: đóng vai trò là "phong bì thư" chứa nội dung đó.
//  Class EmailEventListener: đóng vai trò là "người đưa thư" — lấy phong bì EmailDetails ra và gửi đi.
}