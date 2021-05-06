package com.pfizer.equip.services.interceptors;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;

import com.pfizer.equip.services.controllers.AbstractServicesController;
import com.pfizer.equip.services.exceptions.AuditRuntimeException;
import com.pfizer.equip.services.exceptions.NotAuthenticatedException;
import com.pfizer.equip.services.exceptions.NotAuthorizedException;
import com.pfizer.equip.shared.service.business.audit.AuditService;
import com.pfizer.equip.shared.service.business.audit.input.AuditEntryInput;
import com.pfizer.equip.shared.service.user.UserLookupService;
import com.pfizer.equip.shared.service.user.exceptions.NoSuchUserException;
import com.pfizer.equip.shared.types.ActionStatusType;
import com.pfizer.equip.shared.types.EntityType;

public class PrivilegeCheckInterceptor extends BaseInterceptor {
   // This interceptor verifies that a user has a "basic" privilege necessary for accessing a given system.
   // Minimal level of authorization for accessing any REST endpoint.
   @Autowired
   UserLookupService userLookupService;

   @Autowired
   private AuditService auditService;

   @Override
   // TODO: For some reason the interceptors are being called twice.
   public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws ExecutionException {
      String requestUserId = "";
      String systemId = "";
      boolean isValidUser = false;

      if (handler instanceof HandlerMethod) {
         Object controllerBean = ((HandlerMethod) handler).getBean();
         if (controllerBean instanceof AbstractServicesController) {
            requestUserId = getUserId(request);
            // TODO: Get the systemId from the path. Hard coding until all services have the
            // variable present in the path.
            systemId = "nca";

            try {
               isValidUser = userLookupService.isValidUser(systemId, requestUserId);
            } catch (NoSuchUserException nsue) {
               throw new NotAuthenticatedException(String.format("User '%s' not found in directory, this typically happens when SSO is bypassed", requestUserId));
            } catch (IOException | ExecutionException e) {
               throw new RuntimeException(e);
            }
            if (isValidUser) {
               return true;
            } else {
               try {
                  auditService.insertAuditEntry(new AuditEntryInput("Failed Application Authorization.", requestUserId, EntityType.USER.getValue(),
                        requestUserId, ActionStatusType.FAILURE, null));
               } catch (IOException e) {
                  throw new AuditRuntimeException("Exception occured during creation of audit entry of failed application authorization", e);
               }
               throw new NotAuthorizedException(String.format("User %s is not authorized to access system %s", requestUserId, systemId));
            }
         }
      }
      return true;
   }
}