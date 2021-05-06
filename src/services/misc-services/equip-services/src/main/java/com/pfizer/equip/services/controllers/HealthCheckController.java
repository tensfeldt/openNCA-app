package com.pfizer.equip.services.controllers;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jamonapi.MonitorFactory;

@RestController
public class HealthCheckController extends AbstractServicesController {
   private final Logger log = LoggerFactory.getLogger(this.getClass());

   @RequestMapping("health-check")
   public String healthCheck() throws UnknownHostException {
      log.debug("Hitting health check URL.");
      String hostName = "Hostname: " + InetAddress.getLocalHost().getHostName();
      String htmlTable = MonitorFactory.getReport();
      return hostName + "<p>" + htmlTable;
   }
}
