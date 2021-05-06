package com.pfizer.equip.processors.impl;

import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.quartz.DisallowConcurrentExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import com.pfizer.equip.processors.framework.Processor;
import com.pfizer.equip.processors.properties.ProcessorsProperties;
import com.pfizer.equip.shared.opmeta.StudyBlindingStatusProcessorService;
import com.pfizer.equip.shared.properties.SharedApplicationProperties;
import com.pfizer.equip.shared.relational.entity.OperationalMetadataJob;
import com.pfizer.equip.shared.relational.repository.OperationalMetadataJobRepository;
import com.pfizer.equip.shared.service.business.audit.AuditService;
import com.pfizer.equip.shared.service.business.audit.input.AuditEntryInput;
import com.pfizer.equip.shared.types.ActionStatusType;
import com.pfizer.equip.shared.types.EntityType;

@DisallowConcurrentExecution
public class StudyBlindingStatusProcessor extends Processor {
   private static final Logger logger = LoggerFactory.getLogger(StudyBlindingStatusProcessor.class);
   private volatile boolean stop = false;
   private static final String SYSTEM_USER = AuditService.SYSTEM_USER;
   private static final String SYSTEM_ID = "nca";

   @Autowired
   StudyBlindingStatusProcessorService studyBlindingStatusProcessorService;

   @Autowired
   OperationalMetadataJobRepository jobRepository;
   OperationalMetadataJob job;

   @Autowired
   private AuditService auditService;

   @Autowired
   private RestTemplateBuilder restTemplateBuilder;
   private RestTemplate restTemplate;
   private String opmetaClearCacheUrl;

   @Autowired
   private ProcessorsProperties processorProperties;

   @Autowired
   private SharedApplicationProperties sharedProperties;

   @Override
   public void stop() {
      stop = true;
   }

   @Override
   public void run(Map<String, Object> properties) {
      try {
         logger.info("Starting operational metadata study blinding status job.");
         job = new OperationalMetadataJob();
         jobRepository.save(job);

         // call GRAABS
         boolean performedUpdate = studyBlindingStatusProcessorService.updateStudyBlindingStatus();

         job.setStatus("SUCCESS_GRAABS");
         job.setEndDate(new Date());
         jobRepository.save(job);

         // Create audit entry
         auditService.insertAuditEntry(new AuditEntryInput("Automatic update performed for GRAABS study blinding status", job.getOperationalMetadataJobId().toString(),
               EntityType.DATALOAD.getValue(), SYSTEM_USER, ActionStatusType.SUCCESS, null));

         if (performedUpdate) {
            // Clear cache
            restTemplate = restTemplateBuilder.build();
            opmetaClearCacheUrl = processorProperties.getServicesBaseUrl() + SYSTEM_ID + "/opmeta/cache/clear";
            HttpHeaders headers = new HttpHeaders();
            headers.add(sharedProperties.getUserIdHeader(), sharedProperties.getServicesServiceAccount());
            restTemplate.exchange(opmetaClearCacheUrl, HttpMethod.POST, new HttpEntity<>(headers), String.class);

         }
         logger.info("Completed operational metadata study blinding status update job.");
      } catch (Exception e) {
         job.setStatus("FAILURE_GRAABS");
         job.setEndDate(new Date());
         job.setError(ExceptionUtils.getStackTrace(e));
         jobRepository.save(job);
         logger.error("Exception occured during operational metadata study blinding status job.", e);
         throw new RuntimeException(e);
      }
   }
}
