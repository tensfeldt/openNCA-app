package com.pfizer.equip.services;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.pfizer.equip.services.interceptors.PrivilegeCheckInterceptor;
import com.pfizer.equip.services.interceptors.UserIdHeaderInterceptor;

/**
 * 
 * This class is to configure any Spring MVC related options that need to be set.
 * Techniques adapted from the following pages:
 * - https://stackoverflow.com/questions/13000754/spring-3-1-2-mvc-exceptionhandler-with-responsebody/30335453
 * - https://stackoverflow.com/questions/7197268/spring-mvc-httpmediatypenotacceptableexception/8646183
 *
 */
@Configuration
public class MvcConfiguration implements WebMvcConfigurer {
   @Override
   public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
      // this call will force Spring to not ignore the Accept response header on HTTP GET requests
      // for REST URLs that have a filename based path (e.g. /somefile.txt)
      configurer.favorPathExtension(false);
   }
   
   // this method will tell Spring to create the interceptor as a Spring bean so that dependency injection will work inside it
   @Bean
   UserIdHeaderInterceptor localUserIdHeaderInterceptor() {
      return new UserIdHeaderInterceptor(); 
   }
   @Bean
   PrivilegeCheckInterceptor localPrivilegeCheckInterceptor() {
      return new PrivilegeCheckInterceptor(); 
   }
   

   // add any pre or post interceptors the application requires
   @Override
   public void addInterceptors(InterceptorRegistry registry) {
      // this will set the user ID from the HTTP header into the current request controller's context
      registry.addInterceptor(localUserIdHeaderInterceptor());
      // this will check that the user has a LOGON privilege for the requested systemId
      registry.addInterceptor(localPrivilegeCheckInterceptor());
   }
}
