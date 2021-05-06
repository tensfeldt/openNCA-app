package com.pfizer.equip.services.interceptors;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.pfizer.equip.services.exceptions.AuditRuntimeException;
import com.pfizer.equip.services.exceptions.NotAuthenticatedException;
import com.pfizer.equip.services.properties.ApplicationProperties;
import com.pfizer.equip.shared.service.business.audit.AuditService;
import com.pfizer.equip.shared.service.business.audit.input.AuditEntryInput;
import com.pfizer.equip.shared.types.ActionStatusType;
import com.pfizer.equip.shared.types.EntityType;

public class UserIdHeaderInterceptor extends BaseInterceptor {
   @Autowired
   Environment environment;
   
   @Autowired
   ApplicationProperties applicationProperties;

   @Autowired
   private AuditService auditService;
   private final Logger log = LoggerFactory.getLogger(this.getClass());

   @Override
   public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
      // Adding exception to interceptors for Swagger requests except in production
      boolean isProd = Arrays.asList(environment.getActiveProfiles()).contains("prod");
      if (!isProd && (request.getRequestURL().toString().contains("/v2/api-docs") || request.getRequestURL().toString().contains("swagger"))) {
         return true;
      }
      
      if (applicationProperties.isStandaloneMode()) {
         return true;
      }

      String userId = getUserId(request);
      if (userId == null || userId.trim().isEmpty()) {
         try {
            // Null value is passed as user id and Unknown as entity Id. Failed login typically occurs here because there is no user Id in header.
            // TODO: Failed Login to be handled at SSO Level.
            auditService.insertAuditEntry(
                  new AuditEntryInput("Failed Login Attempt.", "(unknown)", EntityType.USER.getValue(), null, ActionStatusType.FAILURE, null), false);
         } catch (Exception e) {
            String message = "Exception occured during creation of audit entry of failed login";
            log.error(message, e);
            throw new AuditRuntimeException(message, e);
         }
         throw new NotAuthenticatedException(
               String.format("User ID header '%s' is missing or blank, which typically only happens when SSO is bypassed", applicationProperties.getUserIdHeader()));
      }
      return true;
   }
}
