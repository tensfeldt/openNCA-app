package com.pfizer.equip.services.config;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * 
 * This class allows us to invoke any app initialization logic once Spring Boot has initialized.
 * We can use it to call our log4j configuration method to load the correct log4j configuration based on the currently active Spring profile.
 */
@Component
public class ApplicationStartup implements ApplicationListener<ContextRefreshedEvent> {

   @Override
   public void onApplicationEvent(ContextRefreshedEvent arg0) {
      // TODO: place any application start up logic here
   }
}
