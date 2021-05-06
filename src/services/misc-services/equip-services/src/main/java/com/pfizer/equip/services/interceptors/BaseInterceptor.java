package com.pfizer.equip.services.interceptors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.pfizer.equip.services.properties.ApplicationProperties;

public abstract class BaseInterceptor extends HandlerInterceptorAdapter {
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
}
