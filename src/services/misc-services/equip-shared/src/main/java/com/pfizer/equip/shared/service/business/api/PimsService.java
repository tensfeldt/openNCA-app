package com.pfizer.equip.shared.service.business.api;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.pfizer.equip.shared.properties.SharedApplicationProperties;

@Service
public class PimsService {

   private final Logger log = LoggerFactory.getLogger(this.getClass());

   @Autowired
   private RestTemplateBuilder restTemplateBuilder;

   @Autowired
   private SharedApplicationProperties properties;
   
   private RestTemplate restTemplate;
   private String pimsStudyStatusUrl;
   
   private static final String SYSTEM_ID = "nca";
   
   public static final String PIMS = "\"PIMS\"";

   @PostConstruct
   private void initialize() {
      restTemplate = restTemplateBuilder.build();
      pimsStudyStatusUrl = properties.getDataframeBaseUrl() + SYSTEM_ID + "/pims/%s/studypimsstatus";
   }
   
   public boolean isPims(String studyId) {
      String url = String.format(pimsStudyStatusUrl, studyId);
      HttpHeaders headers = new HttpHeaders();
      headers.add(properties.getUserIdHeader(), properties.getServicesServiceAccount());
      String studyType = null;
      try {
         studyType = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class).getBody();
      } catch (HttpStatusCodeException e) {
         log.error("Retrieving PIMS status for study {} failed", studyId);
         handleHttpError(e);
      }
      return studyType.equals(PIMS) ? true : false;
   }

   private void handleHttpError(HttpStatusCodeException e) {
      if (e instanceof HttpClientErrorException) {
         throw new HttpClientErrorException(e.getStatusCode(), "Dataframe service returned 4xx (client error): " + e.getResponseBodyAsString());
      } else if (e instanceof HttpServerErrorException) {
         throw new HttpServerErrorException(e.getStatusCode(), "Dataframe service returned 5xx (server error): " + e.getResponseBodyAsString());
      } else {
         throw e;
      }
   }

}