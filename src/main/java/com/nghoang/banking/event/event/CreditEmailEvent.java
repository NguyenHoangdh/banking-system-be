package com.nghoang.banking.event;

import com.nghoang.banking.dto.EmailDetails;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CreditEmailEvent extends ApplicationEvent {
    private final EmailDetails creditAlert;

    public CreditEmailEvent(Object source, EmailDetails creditAlert) {
        super(source);
        this.creditAlert = creditAlert;
    }
}
