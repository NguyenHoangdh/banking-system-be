package com.nghoang.banking.event.event;

import com.nghoang.banking.dto.EmailDetails;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TransferEmailEvent extends ApplicationEvent {
    private final EmailDetails debitAlert;
    private final EmailDetails creditAlert;

    public TransferEmailEvent(Object source, EmailDetails debitAlert, EmailDetails creditAlert) {
        super(source);
        this.debitAlert = debitAlert;
        this.creditAlert = creditAlert;
    }
}
