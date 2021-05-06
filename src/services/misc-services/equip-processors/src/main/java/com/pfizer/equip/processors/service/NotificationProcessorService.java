package com.pfizer.equip.processors.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pfizer.equip.processors.properties.ProcessorsProperties;
import com.pfizer.equip.shared.contentrepository.ContentInfo;
import com.pfizer.equip.shared.relational.entity.Event;
import com.pfizer.equip.shared.relational.entity.Notification;
import com.pfizer.equip.shared.relational.entity.Subscription;
import com.pfizer.equip.shared.relational.repository.LockControlRepository;
import com.pfizer.equip.shared.relational.repository.NotificationRepository;
import com.pfizer.equip.shared.relational.repository.SubscriptionRepository;
import com.pfizer.equip.shared.service.user.UserLookupService;

@Service
public class NotificationProcessorService {

   private static final Logger logger = LoggerFactory.getLogger(NotificationProcessorService.class);

   @Autowired
   private NotificationRepository notificationRepository;

   @Autowired
   private LockControlRepository lockControlRepository;

   @Autowired
   private TemplateService templateService;

   @Autowired
   private UserLookupService userLookupService;

   @Autowired
   private EmailService email;

   @Autowired
   private ProcessorsProperties processorProperties;
   
   @Autowired
   private SubscriptionRepository subscriptionRepository;

   @Transactional
   public boolean lock(String lockName) {
      return 1 == lockControlRepository.tryLock(lockName);
   }

   @Transactional
   public void unlock(String lockName) {
      lockControlRepository.unlock(lockName);
   }

   @Transactional
   public void saveNotification(Notification notification) {
      notificationRepository.save(notification);
     
   }

   @Transactional
   public void sendRealtimeEmailAndFlagNotification(Notification notification) {
      sendEmailAndFlagNotifications(Arrays.asList(notification), processorProperties.getMailSubjectPrefix() + " - EQuIP Notification");
   }

   @Transactional
   public void sendDailyEmailAndFlagNotification(List<Notification> notifications) {
      sendEmailAndFlagNotifications(notifications, processorProperties.getMailSubjectPrefix() + " - EQuIP Daily Notification");
   }

   @Transactional
   public void sendWeeklyEmailAndFlagNotification(List<Notification> notifications) {
      sendEmailAndFlagNotifications(notifications, processorProperties.getMailSubjectPrefix() + " - EQuIP Weekly Notification");
   }

   @Transactional
   private void sendEmailAndFlagNotifications(List<Notification> notifications, String subject) {
      // All notifications have the same Subscription. We just need to check the first
      // Notification
      // in the list to retrieve the user id and additional emails.
      try {
         String emailContent = templateService.processEmailTemplate(notifications);
         String emailTo = userLookupService.lookupUser(notifications.get(0).getSubscription().getUserId()).getEmailAddress();
         String additionalEmails = notifications.get(0).getSubscription().getEmail();
         if (StringUtils.isNotBlank(additionalEmails)) {
            emailTo = emailTo + "," + additionalEmails;
         }
         
         List<ContentInfo> attachments = new ArrayList<ContentInfo>();
         logger.info("Saving processed notifications to database");
         
         int attachmentSize = 0;
         for (Notification n : notifications) {
            n.setProcessed(Boolean.TRUE);
            n.setProcessedOn(new Date());
            Event e = n.getEvent();
            if (e.getAttachmentContent() != null) {
               attachmentSize += e.getAttachmentContent().length;
               attachments.add(new ContentInfo(e.getAttachmentContent(), e.getAttachmentMimeType(), e.getAttachmentFilename()));
            }
         }
         notificationRepository.saveAll(notifications);

         logger.info("Sending email notification to {}", emailTo);
         if (attachmentSize <= processorProperties.getMailSizeLimit()) {
            // Less than the limit already, send the attachments uncompressed
            email.send(processorProperties.getMailServiceAccount(), emailTo, subject, emailContent, attachments);
         } else {
            ContentInfo zip = zipAttachments(attachments);
            if (zip.getContent().length <= processorProperties.getMailSizeLimit()) {
               // Less than the limit after compression, send the zip
               email.send(processorProperties.getMailServiceAccount(), emailTo, subject, emailContent, Arrays.asList(zip));
            } else {
               emailContent = "Warning: Attachments not included because they were too large, even with compression\n\n" + emailContent;
               logger.warn("Attachments too large to include with email for notification IDs, even with compression: {}",
                     notifications.stream().map(Notification::getNotificationId).collect(Collectors.toList()));
               email.send(processorProperties.getMailServiceAccount(), emailTo, subject, emailContent);
            }
         }
      } catch (MailException e) {
         throw e;
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   private ContentInfo zipAttachments(List<ContentInfo> attachments) throws IOException {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      ZipOutputStream zip = new ZipOutputStream(byteArrayOutputStream);
      for (ContentInfo attachment : attachments) {
         zip.putNextEntry(new ZipEntry(attachment.getFileName()));
         zip.write(attachment.getContent());
         zip.closeEntry();
      }
      zip.close();
      return new ContentInfo(byteArrayOutputStream.toByteArray(), "application/zip", "attachments.zip");
   }

   public List<Notification> getNotifications(String notificationTypeName, Boolean processed) {
      return notificationRepository.findByNotificationType_NotificationTypeNameAndProcessedOrderBySubscription_SubscriptionId(notificationTypeName,
            processed);
   }
   
   public boolean subscriptionExists(Subscription subscription) {
      if (subscription == null) return false;
      return subscriptionRepository.existsById(subscription.getSubscriptionId());
   }

}
