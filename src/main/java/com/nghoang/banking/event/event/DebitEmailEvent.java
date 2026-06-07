package com.nghoang.banking.event;

import com.nghoang.banking.dto.EmailDetails;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class DebitEmailEvent extends ApplicationEvent {
    private final EmailDetails debitAlert;


    public DebitEmailEvent(Object source, EmailDetails debitAlert) {
        super(source);
        this.debitAlert = debitAlert;
    }
}
