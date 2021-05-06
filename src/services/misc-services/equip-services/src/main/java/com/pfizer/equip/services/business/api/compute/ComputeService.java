package com.pfizer.equip.services.business.api.compute;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfizer.equip.services.business.api.exceptions.RemoteClientErrorException;
import com.pfizer.equip.services.business.api.exceptions.RemoteServerErrorException;
import com.pfizer.equip.services.business.api.input.ComputeInput;
import com.pfizer.equip.services.business.api.input.GenericInput;
import com.pfizer.equip.services.business.api.input.KeyValuePairInput;
import com.pfizer.equip.services.business.api.response.ComputeResponse;
import com.pfizer.equip.services.properties.ApplicationProperties;;

@Service
public class ComputeService {
   public static final String CONTAINER_R_BASE = "equip-r-base";
   public static final String CONTAINER_R_MARKDOWN = "equip-r-markdown";
   public static final String CONTAINER_R_OPENNCA = "equip-opennca";

   private final Logger log = LoggerFactory.getLogger(this.getClass());

   @Autowired
   private ApplicationProperties properties;

   @Autowired
   private RestTemplateBuilder restTemplateBuilder;

   @Autowired
   ObjectMapper mapper;

   private RestTemplate restTemplate;
   private String computeServiceUrl;

   // TODO: remove hardcoding
   private static final String SYSTEM_ID = "nca";

   @PostConstruct
   private void initialize() {
      restTemplate = restTemplateBuilder.build();
      computeServiceUrl = properties.getComputeBaseUrl() + SYSTEM_ID + "/compute";
   }

   // Overloaded call to support not passing dataframe or assembly IDs
   public ComputeResponse compute(String requestUserId, String computeContainer, String scriptId, Set<? extends GenericInput> parameters, String dataframeType,
         String equipId) throws IOException {
      return compute(requestUserId, computeContainer, scriptId, parameters, null, null, dataframeType, equipId, false);
   }

   // Overloaded call to support not passing assembly IDs
   public ComputeResponse compute(String requestUserId, String computeContainer, String scriptId, Set<? extends GenericInput> parameters, List<String> dataframeIds,
         String dataframeType, String equipId) throws IOException {
      return compute(requestUserId, computeContainer, scriptId, parameters, dataframeIds, null, dataframeType, equipId, false, Optional.empty());
   }

   // Overloaded call to support not passing equipId, non-virtual
   public ComputeResponse compute(String requestUserId, String computeContainer, String scriptId, Set<KeyValuePairInput> parameters,
         List<String> dataframeIds, List<String> assemblyIds, String dataframeType) throws IOException {
      return compute(requestUserId, computeContainer, scriptId, parameters, dataframeIds, assemblyIds, dataframeType, null, false, Optional.empty());
   }

   // Overloaded call to assume non-virtual
   public ComputeResponse compute(String requestUserId, String computeContainer, String scriptId, Set<? extends GenericInput> parameters, List<String> dataframeIds,
         List<String> assemblyIds, String dataframeType, String equipId) throws IOException {
      return compute(requestUserId, computeContainer, scriptId, parameters, dataframeIds, assemblyIds, dataframeType, equipId, false, Optional.empty());
   }

   public ComputeResponse compute(String requestUserId, String computeContainer, String scriptId, Set<? extends GenericInput> parameters, List<String> dataframeIds,
         List<String> assemblyIds, String dataframeType, String equipId, boolean isVirtual) throws IOException {
      return compute(requestUserId, computeContainer, scriptId, parameters, dataframeIds, assemblyIds, dataframeType, equipId, isVirtual, Optional.empty());
   }

   public ComputeResponse compute(String requestUserId, String computeContainer, String scriptId, Set<KeyValuePairInput> parameters,
         List<String> dataframeIds, List<String> assemblyIds, String dataframeType, String equipId, String subType) throws IOException {
      return compute(requestUserId, computeContainer, scriptId, parameters, dataframeIds, assemblyIds, dataframeType, equipId, false, Optional.of(subType));
   }

   // List of dataframes must be ordered
   public ComputeResponse compute(String requestUserId, String computeContainer, String scriptId, Set<? extends GenericInput> parameters, List<String> dataframeIds,
         List<String> assemblyIds, String dataframeType, String equipId, boolean isVirtual, Optional<String> subType) throws IOException {
      String environment = "Server";
      ComputeInput computeInput;
      if (subType.isPresent()) {
           computeInput = new ComputeInput(requestUserId, computeContainer, environment, scriptId, parameters, dataframeIds, assemblyIds, dataframeType, equipId, subType.get());
      } else {
           computeInput = new ComputeInput(requestUserId, computeContainer, environment, scriptId, parameters, dataframeIds, assemblyIds, dataframeType, equipId);
      }

      String jsonValue = mapper.writeValueAsString(computeInput);
      HttpHeaders headers = new HttpHeaders();
      MediaType mediaType = new MediaType("application", "json");
      headers.setContentType(mediaType);
      headers.add(properties.getUserIdHeader(), requestUserId);
      HttpEntity<String> entity = new HttpEntity<String>(jsonValue, headers);

      String computeServiceOutput = null;
      try {
         // Do this as a string first in case we get an HTML error response.
         String url = !isVirtual ? computeServiceUrl : computeServiceUrl + "?virtual=true";
         computeServiceOutput = restTemplate.postForObject(url, entity, String.class);
      } catch (HttpStatusCodeException e) {
         log.error("Compute job failed.");
         handleHttpError(e);
      }
      // De-serialize only after success.
      ComputeResponse computeResponse = mapper.readValue(computeServiceOutput, ComputeResponse.class);
      return computeResponse;
   }

   private void handleHttpError(HttpStatusCodeException e) {
      if (e instanceof HttpClientErrorException) {
         throw new RemoteClientErrorException("Compute service returned 4xx (client error): " + e.getResponseBodyAsString());
      } else if (e instanceof HttpServerErrorException) {
         throw new RemoteServerErrorException("Compute service returned 5xx (server error): " + e.getResponseBodyAsString());
      } else {
         throw e;
      }
   }
}