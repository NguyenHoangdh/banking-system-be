package com.nghoang.banking.event.listener;

import com.nghoang.banking.event.event.TransferEmailEvent;
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
    }//Nếu không dùng TransferEmailEvent, tại tầng TransferService thì sẽ phải build 2 DTO EmailDetails và gọi publishEvent() 2 lần. Việc này làm tầng Service bị rác code.

    //Bằng cách tạo TransferEmailEvent chứa cả 2 alert (creditAlert và debitAlert), tầng Service chỉ cần bắn 1 event duy nhất. Listener riêng sẽ chịu trách nhiệm bóc tách và gửi 2 email đó.
    //bên cạnh đó tránh race condition vì 2 email này sẽ được đẩy vào Thread Pool một cách rời rạc. Nếu Thread Pool bị nghẽn hoặc gặp lỗi kết nối SMTP giữa chừng, có thể xảy ra kịch bản người chuyển nhận được mail trừ tiền nhưng người nhận mãi không nhận được mail cộng tiền (hoặc ngược lại)

    //1 sự kiện sinh ra nhiều email hoặc tác vụ phụ khác như lưu biến động số dư, bắn thông báo ở app, tích điểm thưởng,... => đóng gói toàn bộ ngữ cảnh giao dịch vào 1 object duy nhất
}
