package com.pfizer.equip.processors.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfizer.equip.shared.relational.entity.Notification;

@Service
public class TemplateService {
   
   @Autowired
   private SpringTemplateEngine engine;
   
   private ObjectMapper mapper;
   private FastDateFormat dateFormatter;
   private Pattern pattern;
   
   @PostConstruct
   private void initialize() {
      mapper = new ObjectMapper();
      dateFormatter = FastDateFormat.getInstance("MM/dd/yyyy HH:mm:ss");
      pattern = Pattern.compile("(?m)^[ \t]*\r?\n");  //remove empty lines
   }
   
   public String processEmailTemplate(Notification notification) {
      return processEmailTemplate(Arrays.asList(notification));
   }
   
   public String processEmailTemplate(List<Notification> notifications) {
      StringBuilder builder = new StringBuilder();
      TypeReference<Map<String, Object>> reference = new TypeReference<Map<String, Object>>() {};
      for (Notification notification : notifications) {
         Context context = new Context();
         context.setVariable("event", notification.getEvent());
         Date notificationTimeStamp = new Date();
         context.setVariable("notification_timestamp", dateFormatter.format(notificationTimeStamp));
         context.setVariable("event_timestamp",dateFormatter.format(notification.getEvent().getCreatedOn()));
         String descriptions = notification.getEvent().getDescription();
         if (StringUtils.isNotBlank(descriptions)) {
            Map<String, Object> eventDetail;
            try {
               eventDetail = mapper.readValue(descriptions, reference);
            } catch (IOException e) {
               throw new RuntimeException(e);
            }
            context.setVariables(eventDetail);
         }
         String templateOutout = "";
         if (notification.getEvent().getEventType().getGlobalFlag()) {
            templateOutout = pattern.matcher(engine.process("email-global", context)).replaceAll("");
         } else {
            templateOutout = pattern.matcher(engine.process("email", context)).replaceAll("");
         }
         builder.append(templateOutout).append("\n\n");
      }
      return builder.toString();
   }
}
