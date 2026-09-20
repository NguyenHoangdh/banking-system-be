package com.nghoang.banking.service.impl;

import com.nghoang.banking.dto.EmailDetails;
import com.nghoang.banking.exception.AppException;
import com.nghoang.banking.exception.ErrorCode;
import com.nghoang.banking.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmailServiceImpl implements EmailService {
    final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    String senderEmail;

    @Override
    public void sendEmail(EmailDetails emailDetails) {
        try{
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(senderEmail);
            mailMessage.setTo(emailDetails.getRecipient());
            mailMessage.setText(emailDetails.getMessageBody());
            mailMessage.setSubject(emailDetails.getSubject());
            javaMailSender.send(mailMessage);
            log.info("Mail send successfully!");
        } catch (MailException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendEmailWithAttachment(EmailDetails emailDetails) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper;
        try {
            mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setFrom(senderEmail);
            mimeMessageHelper.setTo(emailDetails.getRecipient());
            mimeMessageHelper.setText(emailDetails.getMessageBody());
            mimeMessageHelper.setSubject(emailDetails.getSubject());


            FileSystemResource file = new FileSystemResource(new File(emailDetails.getAttachment()));
            if (!file.exists()) {
                throw new AppException(ErrorCode.ATTACHMENT_NOT_FOUND);
            }
            mimeMessageHelper.addAttachment(file.getFilename(), file);
            javaMailSender.send(mimeMessage);

            log.info("{} has sent to user with email {}", file.getFilename(), emailDetails.getRecipient());
        } catch (MessagingException | MailException  e) {
            throw new RuntimeException(e);
        }
    }
}
