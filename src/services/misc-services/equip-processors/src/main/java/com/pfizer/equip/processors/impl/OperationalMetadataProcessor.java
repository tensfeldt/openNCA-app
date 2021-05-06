package com.pfizer.equip.processors.impl;

import java.util.Calendar;
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
import com.pfizer.equip.shared.opmeta.OperationalMetadataProcessorService;
import com.pfizer.equip.shared.properties.SharedApplicationProperties;
import com.pfizer.equip.shared.relational.entity.OperationalMetadataJob;
import com.pfizer.equip.shared.relational.repository.OperationalMetadataJobRepository;
import com.pfizer.equip.shared.service.business.audit.AuditService;
import com.pfizer.equip.shared.service.business.audit.input.AuditEntryInput;
import com.pfizer.equip.shared.types.ActionStatusType;
import com.pfizer.equip.shared.types.EntityType;

@DisallowConcurrentExecution
public class OperationalMetadataProcessor extends Processor {

   private static final Logger logger = LoggerFactory.getLogger(OperationalMetadataProcessor.class);
   private volatile boolean stop = false;
   private static final int MAX_ERRORS = 10; // consecutive errors
   private static final String SYSTEM_USER = AuditService.SYSTEM_USER;
   private static final String SYSTEM_ID = "nca";

   @Autowired
   private OperationalMetadataProcessorService operationalMetadataProcessorService;

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
         logger.info("Starting operational metadata ETL job.");
         Date extractionDate;
         OperationalMetadataJob lastSuccessfulJob = jobRepository.findFirstByStatusOrderByStartDateDesc("SUCCESS_OPMETA");
         if (lastSuccessfulJob != null) {
            // incremental run
            extractionDate = lastSuccessfulJob.getStartDate();
         } else {
            // first run, full load
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, 1900);
            calendar.set(Calendar.MONTH, Calendar.JANUARY);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            extractionDate = calendar.getTime();
         }
         job = new OperationalMetadataJob();
         jobRepository.save(job);

         logger.info("Starting operationa metadata filter analysis.");
         operationalMetadataProcessorService.analyze();

         logger.info("Starting operational metadata extraction.");
         operationalMetadataProcessorService.extract(extractionDate);

         logger.info("Starting operational metadata load.");
         operationalMetadataProcessorService.load();

         job.setStatus("SUCCESS_OPMETA");
         job.setEndDate(new Date());
         jobRepository.save(job);

         // Create audit entry
         auditService.insertAuditEntry(new AuditEntryInput("Automatic data load performed for PODS sourced entities", job.getOperationalMetadataJobId().toString(),
               EntityType.DATALOAD.getValue(), SYSTEM_USER, ActionStatusType.SUCCESS, null));

         // Clear cache
         restTemplate = restTemplateBuilder.build();
         opmetaClearCacheUrl = processorProperties.getServicesBaseUrl() + SYSTEM_ID + "/opmeta/cache/clear";
         HttpHeaders headers = new HttpHeaders();
         headers.add(sharedProperties.getUserIdHeader(), sharedProperties.getServicesServiceAccount());
         restTemplate.exchange(opmetaClearCacheUrl, HttpMethod.POST, new HttpEntity<>(headers), String.class);

         logger.info("Completed operational metadata ETL job.");
      } catch (Exception e) {
         job.setStatus("FAILURE_OPMETA");
         job.setEndDate(new Date());
         job.setError(ExceptionUtils.getStackTrace(e));
         jobRepository.save(job);
         logger.error("Exception occured during operational metadata ETL job.", e);
         throw new RuntimeException(e);
      }
   }
}
