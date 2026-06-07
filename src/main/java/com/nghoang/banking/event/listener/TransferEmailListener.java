package com.nghoang.banking.event;

import com.nghoang.banking.dto.EmailDetails;
import com.nghoang.banking.service.impl.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Component
@RequiredArgsConstructor
public class TransferEmailListener {
    private final EmailService emailService;


    @Async //@Async giúp gửi email không block response trả về client. Nếu dùng @Async thì cần thêm @EnableAsync vào class config, @Async nghĩa là method này chạy trên 1 thread riêng biệt, ko chờ nó xong mới trả về response về client
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) //điểm mấu chốt, bảo Spring: "đợi transaction xong hẳn, nếu commit thì mới gọi method này, rollback thì bỏ qua"
    public void onTransferCompleted(TransferEmailEvent transferEmailEvent) {
        emailService.sendEmail(transferEmailEvent.getCreditAlert());
        emailService.sendEmail(transferEmailEvent.getDebitAlert());
    }
}
