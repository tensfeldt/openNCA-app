package com.pfizer.equip.processors;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.quartz.ee.servlet.QuartzInitializerListener;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.RequestMapping;

import com.pfizer.equip.processors.framework.ProcessorInitializer;
import com.pfizer.equip.processors.properties.ProcessorsProperties;

@SpringBootApplication(scanBasePackages = { "com.pfizer.equip" })
@EnableConfigurationProperties(ProcessorsProperties.class)
@EnableTransactionManagement
public class Application extends SpringBootServletInitializer {

   public SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
      return builder.sources(Application.class);
   }

   @Bean
   public ServletContextInitializer initializer() {
      return new ServletContextInitializer() {

         @Override
         public void onStartup(ServletContext servletContext) throws ServletException {
            servletContext.addListener(new QuartzInitializerListener());
            servletContext.addListener(new ProcessorInitializer());
            servletContext.setInitParameter("quartz:shutdown-on-unload", "true");
            servletContext.setInitParameter("quartz:wait-on-shutdown", "true");
            servletContext.setInitParameter("quartz:start-scheduler-on-load", "true");
         }
      };
   }

   @Controller
   class ApplicationController {

      @RequestMapping("/")
      public String index() {
         return "index";
      }
   }

}
