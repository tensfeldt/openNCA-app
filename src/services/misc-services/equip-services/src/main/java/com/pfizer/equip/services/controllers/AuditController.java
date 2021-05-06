package com.pfizer.equip.services.controllers;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.jamonapi.Monitor;
import com.pfizer.equip.services.exceptions.AuditRuntimeException;
import com.pfizer.equip.services.exceptions.NotAuthorizedException;
import com.pfizer.equip.services.loggers.Performance;
import com.pfizer.equip.services.responses.AuditEntryResponse;
import com.pfizer.equip.shared.responses.AuditHistoryResponse;
import com.pfizer.equip.shared.responses.Response;
import com.pfizer.equip.shared.service.business.audit.AuditService;
import com.pfizer.equip.shared.service.business.audit.input.AuditEntryInput;
import com.pfizer.equip.shared.service.business.audit.input.AuditFilterInput;
import com.pfizer.equip.shared.service.user.PrivilegeType;
import com.pfizer.equip.shared.service.user.UserLookupService;

@RestController
public class AuditController extends AbstractServicesController {
   private final Logger log = LoggerFactory.getLogger(this.getClass());
   private final Logger perfLog = LoggerFactory.getLogger(Performance.class);
   public static final String SYSTEM_ID_NCA = "nca";

   @Autowired
   private AuditService auditService;

   @Autowired
   private UserLookupService userLookupService;

   @RequestMapping(value = "{systemId}/audit-entry", method = RequestMethod.POST)
   public AuditEntryResponse addAuditEntry(@PathVariable String systemId, @RequestBody AuditEntryInput input, HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userIdFromHeader = getUserId(request);

         if (input.getUserId() != null && !userIdFromHeader.equalsIgnoreCase(input.getUserId())) {
            // FIXME remove this if statement following service account resolution
            if (!(userIdFromHeader.equalsIgnoreCase("SRVAMR-EQUIPPRD") || userIdFromHeader.equalsIgnoreCase("SRVAMR-EQNONPRD1")
                  || userIdFromHeader.equalsIgnoreCase("SRVAMR-EQNONPRD2"))) {
               throw new AuditRuntimeException(String.format("User '%s' is not authorized to create an audit entry of user '%s'.", userIdFromHeader, input.getUserId()));
            }
            // throw new AuditRuntimeException(String.format("User '%s' is not authorized to create an audit entry of user '%s'.", userIdFromHeader, input.getUserId()));
         }
         long auditEntryId = auditService.insertAuditEntry(input, systemId);

         // TODO: need to determine what kind of response the front end requires
         AuditEntryResponse response = new AuditEntryResponse();
         response.setResponse(Response.OK);
         response.setAuditEntryId(auditEntryId);
         return response;
      } catch (Exception e) {
         log.error("Exception occured during creation of audit entry.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }

   @RequestMapping(value = "{systemId}/audit-entries/{entityId}", method = RequestMethod.POST)
   public AuditHistoryResponse searchAuditEntries(@PathVariable String systemId, @PathVariable String entityId, @RequestBody AuditFilterInput input,
         HttpServletRequest request) {
      Monitor monitor = null;
      try {
         String userId = getUserId(request);

         log.info("Searching logs for user ID '{}'...", userId);
         // check for the user privilege to perform this action
         if (!userLookupService.hasPrivilege(userId, SYSTEM_ID_NCA, PrivilegeType.VIEW_AUDIT)) {
            throw new NotAuthorizedException(String.format("Request user '%s' is not authorized to view audit entries.", userId));
         }

         return auditService.getAuditEntriesByEntity(entityId, input);
      } catch (Exception e) {
         log.error("Exception occured searching for audit entries.", e);
         throw new RuntimeException(e);
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }
}
