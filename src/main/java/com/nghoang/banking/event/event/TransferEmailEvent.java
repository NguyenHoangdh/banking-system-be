package com.nghoang.banking.event;

import com.nghoang.banking.dto.EmailDetails;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Clock;

@Getter
public class TransferEmailEvent extends ApplicationEvent {
    private final EmailDetails debitAlert;
    private final EmailDetails creditAlert;

    public TransferEmailEvent(Object source, EmailDetails debitAlert, EmailDetails creditAlert) {
        super(source); //yêu cầu bắt buộc của class cha ApplicationEvent, có nghĩa là ai đã tạo ra event này
        this.debitAlert = debitAlert;
        this.creditAlert = creditAlert;
    }
}
