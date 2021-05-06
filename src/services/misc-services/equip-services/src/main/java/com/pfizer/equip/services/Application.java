package com.pfizer.equip.services;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.pfizer.equip.services.properties.ApplicationProperties;
import com.pfizer.equip.services.properties.ModeShapeServiceProperties;
import com.pfizer.equip.services.properties.ValidationProperties;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

@SpringBootApplication(scanBasePackages = { "com.pfizer.equip" })
@EnableConfigurationProperties({ ApplicationProperties.class, ModeShapeServiceProperties.class, ValidationProperties.class })
@EnableEncryptableProperties
@EnableTransactionManagement
public class Application extends SpringBootServletInitializer {
   public static void main(String[] args) {
      SpringApplication.run(Application.class, args);
   }
}
