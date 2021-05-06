package com.pfizer.equip.services.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.jamonapi.Monitor;
import com.pfizer.equip.services.loggers.Performance;
import com.pfizer.equip.shared.relational.entity.ListName;
import com.pfizer.equip.shared.relational.entity.ListValue;
import com.pfizer.equip.shared.service.list.ListService;

@RestController
public class ListController extends AbstractServicesController {
   private final Logger log = LoggerFactory.getLogger(this.getClass());
   private final Logger perfLog = LoggerFactory.getLogger(Performance.class);

   @Autowired
   private ListService listService;

   @RequestMapping(value = "{systemId}/list/{name}", method = RequestMethod.GET)
   public List<ListValue> getListValuesByName(@PathVariable("name") String name) {
      Monitor monitor = null;
      log.info("Retrieving list values for {}", name);
      try {
         return listService.getList(name);
      } catch (Exception e) {
         log.error("Failed retrieving list values for " + name, e);
         throw e;
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }
   
   @RequestMapping(value = "{systemId}/list", method = RequestMethod.GET)
   public List<ListName> getAllLists() {
      Monitor monitor = null;
      log.info("Retrieving all lists");
      try {
         return listService.getAllLists();
      } catch (Exception e) {
         log.error("Failed retrieving all lists", e);
         throw e;
      } finally {
         if (monitor != null) {
            monitor.stop();
            perfLog.info(monitor.toString());
         }
      }
   }
}
