package com.nghoang.banking.event;

import com.nghoang.banking.dto.EmailDetails;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AccountCreationEmailEvent extends ApplicationEvent {
    private final EmailDetails creationAccountAlert;


    public AccountCreationEmailEvent(Object source, EmailDetails creationAccountAlert) {
        super(source); //yêu cầu bắt buộc của class cha ApplicationEvent, có nghĩa là ai đã tạo ra event này
        this.creationAccountAlert = creationAccountAlert;
    }
}
