package com.pfizer.equip.shared.service.business.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfizer.equip.shared.properties.SharedApplicationProperties;

@Service
public class ReportingEventService {

   private final Logger log = LoggerFactory.getLogger(this.getClass());

   @Autowired
   private RestTemplateBuilder restTemplateBuilder;

   @Autowired
   private SharedApplicationProperties properties;
   
   @Autowired
   private ObjectMapper mapper;

   private RestTemplate restTemplate;
   private String reportingEventStudyUrl;
   private String reportingEventListUrl;
   private String reportingEventItemsUrl;
   
   private static final String SYSTEM_ID = "nca";

   @PostConstruct
   private void initialize() {
      restTemplate = restTemplateBuilder.build();
      reportingEventStudyUrl = properties.getDataframeBaseUrl() + SYSTEM_ID + "/reportingevents/studyids";
      reportingEventListUrl = properties.getDataframeBaseUrl() + SYSTEM_ID + "/reportingevents/list";
      reportingEventItemsUrl = properties.getDataframeBaseUrl() + SYSTEM_ID + "/reportingeventitems/list";
   }

   public Set<ReportingEvent> getReportingEvents(String programCode, String studyId, String requestUserId) {
      Set<String> input = new HashSet<String>();
      input.add(String.format("%s:%s", programCode, studyId));
      ReportingEvent reportingEvents[] = postJson(requestUserId, reportingEventStudyUrl, input, ReportingEvent[].class);
      return new HashSet<ReportingEvent>(Arrays.asList(reportingEvents));
   }

   public ReportingEvent getReportingEventById(String reportingEventId, String requestUserId) {
      return getReportingEventsById(new HashSet<>(Arrays.asList(reportingEventId)), requestUserId).iterator().next();
   }

   public Set<ReportingEvent> getReportingEventsById(Set<String> reportingEventIds, String requestUserId) {
      ReportingEvent reportingEvents[] = postJson(requestUserId, reportingEventListUrl, reportingEventIds, ReportingEvent[].class);
      return new HashSet<ReportingEvent>(Arrays.asList(reportingEvents));
   }
   
   public Set<ReportingEventItem> getReportingEventItems(Set<String> reportingEventItemIds, String requestUserId) {
      ReportingEventItem reportingEvents[] = postJson(requestUserId, reportingEventItemsUrl, reportingEventItemIds, ReportingEventItem[].class);
      return new HashSet<>(Arrays.asList(reportingEvents));
   }
   
   public boolean hasReleasedReportingEvent(String programCode, String studyId, String requestUserId) {
      Set<ReportingEvent> reportingEvents = getReportingEvents(programCode, studyId, requestUserId);
      for (ReportingEvent reportingEvent : reportingEvents) {
         if (reportingEvent.getReportingEventReleaseStatusKey() != null && reportingEvent.getReportingEventReleaseStatusKey().equals("Released")) {
            return true;
         }
      }
      return false;
   }

   private <T> T postJson(String requestUserId, String url, Object object, Class<T> outputClass) {
      HttpHeaders headers = new HttpHeaders();
      headers.add(properties.getUserIdHeader(), requestUserId);
      MediaType mediaType = new MediaType("application", "json");
      headers.setContentType(mediaType);
      String jsonValue;
      try {
         jsonValue = mapper.writeValueAsString(object);
      } catch (Exception e) {
         throw new RuntimeException("Error occured while POST-ing JSON.");
      }
      return restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(jsonValue, headers), outputClass).getBody();
   }

}