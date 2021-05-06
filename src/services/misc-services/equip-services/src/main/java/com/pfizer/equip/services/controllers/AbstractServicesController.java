package com.pfizer.equip.services.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.pfizer.equip.services.properties.ApplicationProperties;
import com.pfizer.equip.services.responses.ExceptionResponse;
import com.pfizer.equip.shared.exceptions.EquipException;

@Component
public abstract class AbstractServicesController {
   @Autowired
   ApplicationProperties applicationProperties;

   protected String getUserId(HttpServletRequest request) {
      if (applicationProperties.isStandaloneMode()) {
         return "standalone-user";
      }
      String headerName = applicationProperties.getUserIdHeader();
      String userId = request.getHeader(headerName);

      return userId;
   }

   private void constructTraceMessage(Throwable e, List<String> stackTrace, Map<String, String> messagesMap, boolean isCause) {
      if (e == null) {
         // base case, return
         return;
      }

      // construct message for this exception and its stack trace
      String prefix = isCause ? "Caused by: " : "";
      stackTrace.add(prefix + e.toString());
      StackTraceElement elements[] = e.getStackTrace();
      for (int i = 0; i < elements.length; i++) {
         StackTraceElement element = elements[i];
         String stackMsg = "at " + element.getClassName() + "." + element.getMethodName() + "(" + element.getFileName() + ":" + element.getLineNumber() + ")";
         if (messagesMap.get(stackMsg) != null) {
            // check to see if this message already exists in the map, if so, we can stop, since it's been logged by a previous parent
            stackTrace.add("... " + (elements.length - i) + " more");
            break;
         }

         // add this message to list and to the map for the next child cause to check
         stackTrace.add(stackMsg);
         messagesMap.put(stackMsg, stackMsg);

      }

      // recursively call this function for the next cause
      constructTraceMessage(e.getCause(), stackTrace, messagesMap, true);
   }

   /**
    * Default exception handler for Spring controller classes. Sub-classes can override as needed.
    */
   @ExceptionHandler(Exception.class)
   @ResponseBody()
   public ResponseEntity<ExceptionResponse> myExceptionHandler(final Exception e) {
      ExceptionResponse response = new ExceptionResponse();
      List<String> stackTrace = response.getStackTrace();

      // call recursive function to construct entire stack trace
      Map<String, String> messagesMap = new HashMap<String, String>();
      constructTraceMessage(e, stackTrace, messagesMap, false);
      
      ResponseEntity<ExceptionResponse> entity;
      if (e instanceof EquipException) {
         entity = new ResponseEntity<ExceptionResponse>(response, getHttpStatus((EquipException)e));
      } else {
         entity = new ResponseEntity<ExceptionResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);
      }
      return entity;
   }
   
   private static HttpStatus getHttpStatus(EquipException e) {
      if (e.getClass().getAnnotation(ResponseStatus.class) != null) {
         return e.getClass().getAnnotation(ResponseStatus.class).value();
      } else {
         return HttpStatus.INTERNAL_SERVER_ERROR;
      }
   }
}
