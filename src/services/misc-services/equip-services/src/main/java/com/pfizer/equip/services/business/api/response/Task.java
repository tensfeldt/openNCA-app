package com.pfizer.equip.services.business.api.response;

import java.util.concurrent.Callable;

import javax.servlet.ServletContext;

import org.springframework.web.context.support.SpringBeanAutowiringSupport;

public abstract class Task<T> implements Callable<T>{
   static ServletContext context;
   
   public Task() {
      SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, context);
   }
   
   abstract T execute();
   
   public T call() {
      return execute();
   }
}
