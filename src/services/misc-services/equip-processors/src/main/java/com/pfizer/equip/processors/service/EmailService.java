package com.pfizer.equip.processors.service;

import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.pfizer.equip.shared.contentrepository.ContentInfo;

@Service
public class EmailService {

   @Autowired
   private JavaMailSender mailSender;

   // from can be a list of comma separated addresses
   public void send(String from, String to, String subject, String body) {
      SimpleMailMessage mailMessage = new SimpleMailMessage();
      mailMessage.setFrom(from);
      mailMessage.setTo(StringUtils.split(to, ","));
      mailMessage.setSubject(subject);
      mailMessage.setText(body);
      mailSender.send(mailMessage);
   }

   public void send(String from, String to, String subject, String body, List<ContentInfo> attachments) throws MessagingException {
      MimeMessage mailMessage = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mailMessage, true);
      helper.setFrom(from);
      helper.setTo(StringUtils.split(to, ","));
      helper.setSubject(subject);
      helper.setText(body);
      for (ContentInfo attachment: attachments) {
         helper.addAttachment(attachment.getFileName(), new ByteArrayResource(attachment.getContent()));
      }
      mailSender.send(mailMessage);
   }
}
